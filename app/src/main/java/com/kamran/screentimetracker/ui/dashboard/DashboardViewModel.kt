package com.kamran.screentimetracker.ui.dashboard

import android.app.usage.UsageEvents
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamran.screentimetracker.data.local.SettingsManager
import com.kamran.screentimetracker.data.local.entity.ScreenUsageEvent
import com.kamran.screentimetracker.data.repository.ScreenTimeRepository
import com.kamran.screentimetracker.util.UsageStatsCalculator
import com.kamran.screentimetracker.widget.WidgetUpdater
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardUiState(
    val totalScreenTimeMillis: Long = 0,
    val unlockCount: Int = 0,
    val sessionCount: Int = 0
)

class DashboardViewModel(
    private val repository: ScreenTimeRepository,
    private val settingsManager: SettingsManager
) : ViewModel() {

    val use24HourFormat = settingsManager.use24HourFormat
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val uiState: StateFlow<DashboardUiState> = repository.getAllEvents()
        .map { events ->
            calculateStats(events)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )

    private fun calculateStats(events: List<ScreenUsageEvent>): DashboardUiState {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayEvents = events.filter { it.timestamp >= today }.sortedBy { it.timestamp }
        
        val totalTime = UsageStatsCalculator.calculateTodayStats(events)
        val unlocks = todayEvents.count { it.eventType == UsageEvents.Event.KEYGUARD_HIDDEN }

        return DashboardUiState(
            totalScreenTimeMillis = totalTime,
            unlockCount = unlocks,
            sessionCount = todayEvents.count { it.eventType == UsageEvents.Event.SCREEN_INTERACTIVE }
        )
    }

    suspend fun sync(context: Context) {
        repository.syncUsageEvents()
        WidgetUpdater.update(context)
    }
}
