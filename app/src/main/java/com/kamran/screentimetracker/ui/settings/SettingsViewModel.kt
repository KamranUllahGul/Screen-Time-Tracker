package com.kamran.screentimetracker.ui.settings

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamran.screentimetracker.data.local.SettingsManager
import com.kamran.screentimetracker.data.local.ThemeConfig
import com.kamran.screentimetracker.data.repository.ScreenTimeRepository
import com.kamran.screentimetracker.data.local.entity.ScreenUsageEvent
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.app.usage.UsageEvents

sealed interface ExportResult {
    data object Idle : ExportResult
    data object Loading : ExportResult
    data class Success(val uri: Uri) : ExportResult
    data class Error(val message: String) : ExportResult
}

class SettingsViewModel(
    private val settingsManager: SettingsManager,
    private val repository: ScreenTimeRepository
) : ViewModel() {

    val themeConfig = settingsManager.themeConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeConfig.FOLLOW_SYSTEM)

    val use24HourFormat = settingsManager.use24HourFormat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _exportResult = MutableStateFlow<ExportResult>(ExportResult.Idle)
    val exportResult: StateFlow<ExportResult> = _exportResult.asStateFlow()

    fun setTheme(theme: ThemeConfig) {
        viewModelScope.launch {
            settingsManager.setThemeConfig(theme)
        }
    }

    fun set24HourFormat(use24Hour: Boolean) {
        viewModelScope.launch {
            settingsManager.setUse24HourFormat(use24Hour)
        }
    }

    fun resetExportResult() {
        _exportResult.value = ExportResult.Idle
    }

    fun exportData(context: Context, format: String) {
        viewModelScope.launch {
            _exportResult.value = ExportResult.Loading
            try {
                val events = repository.getAllEvents().first()
                val uri = withContext(Dispatchers.IO) {
                    performExport(context, events, format)
                }
                if (uri != null) {
                    _exportResult.value = ExportResult.Success(uri)
                } else {
                    _exportResult.value = ExportResult.Error("Failed to create export file")
                }
            } catch (e: Exception) {
                _exportResult.value = ExportResult.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    private fun performExport(context: Context, events: List<ScreenUsageEvent>, format: String): Uri? {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "ScreenTimeUsage_$timestamp.$format"
        val tempFile = File(context.cacheDir, fileName)
        
        return try {
            tempFile.bufferedWriter().use { writer ->
                if (format == "csv") {
                    writer.write("Timestamp,EventType,PackageName\n")
                    events.forEach { event ->
                        val eventName = getEventName(event.eventType)
                        writer.write("${event.timestamp},$eventName,${event.packageName}\n")
                    }
                } else {
                    // Simple JSON manual formatting to avoid adding more libraries
                    writer.write("[\n")
                    events.forEachIndexed { index, event ->
                        val eventName = getEventName(event.eventType)
                        writer.write("  {\n")
                        writer.write("    \"timestamp\": ${event.timestamp},\n")
                        writer.write("    \"eventType\": \"$eventName\",\n")
                        writer.write("    \"packageName\": \"${event.packageName}\"\n")
                        writer.write("  }${if (index < events.size - 1) "," else ""}\n")
                    }
                    writer.write("]")
                }
            }
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun getEventName(type: Int): String {
        return when (type) {
            UsageEvents.Event.SCREEN_INTERACTIVE -> "SCREEN_INTERACTIVE"
            UsageEvents.Event.SCREEN_NON_INTERACTIVE -> "SCREEN_NON_INTERACTIVE"
            UsageEvents.Event.KEYGUARD_HIDDEN -> "KEYGUARD_HIDDEN"
            UsageEvents.Event.KEYGUARD_SHOWN -> "KEYGUARD_SHOWN"
            else -> "UNKNOWN($type)"
        }
    }
}
