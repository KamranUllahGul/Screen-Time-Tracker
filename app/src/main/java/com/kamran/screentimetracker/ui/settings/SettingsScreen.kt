package com.kamran.screentimetracker.ui.settings

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kamran.screentimetracker.data.local.ThemeConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val themeConfig by viewModel.themeConfig.collectAsState()
    val use24HourFormat by viewModel.use24HourFormat.collectAsState()
    val exportResult by viewModel.exportResult.collectAsState()

    LaunchedEffect(exportResult) {
        if (exportResult is ExportResult.Success) {
            val uri = (exportResult as ExportResult.Success).uri
            val format = if (uri.toString().endsWith("csv")) "CSV" else "JSON"
            val mimeType = if (format == "CSV") "text/csv" else "application/json"
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share $format"))
            viewModel.resetExportResult()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SettingsHeader("Appearance")
                
                ThemeSetting(
                    selectedTheme = themeConfig,
                    onThemeSelected = { viewModel.setTheme(it) }
                )

                HorizontalDivider()

                SettingsHeader("Preferences")
                
                ListItem(
                    headlineContent = { Text("24-Hour Format") },
                    supportingContent = { Text("Use 24-hour time format in the app") },
                    leadingContent = { Icon(Icons.Rounded.Schedule, contentDescription = null) },
                    trailingContent = {
                        Switch(
                            checked = use24HourFormat,
                            onCheckedChange = { viewModel.set24HourFormat(it) }
                        )
                    }
                )

                HorizontalDivider()

                SettingsHeader("Data Management")
                
                ListItem(
                    headlineContent = { Text("Export as CSV") },
                    supportingContent = { Text("Export all screen usage events to a CSV file") },
                    leadingContent = { Icon(Icons.Rounded.FileDownload, contentDescription = null) },
                    modifier = Modifier.clickable(enabled = exportResult !is ExportResult.Loading) {
                        viewModel.exportData(context, "csv")
                    }
                )

                ListItem(
                    headlineContent = { Text("Export as JSON") },
                    supportingContent = { Text("Export all screen usage events to a JSON file") },
                    leadingContent = { Icon(Icons.Rounded.FileDownload, contentDescription = null) },
                    modifier = Modifier.clickable(enabled = exportResult !is ExportResult.Loading) {
                        viewModel.exportData(context, "json")
                    }
                )
            }

            if (exportResult is ExportResult.Loading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            
            if (exportResult is ExportResult.Error) {
                val error = (exportResult as ExportResult.Error).message
                AlertDialog(
                    onDismissRequest = { viewModel.resetExportResult() },
                    title = { Text("Export Error") },
                    text = { Text(error) },
                    confirmButton = {
                        TextButton(onClick = { viewModel.resetExportResult() }) {
                            Text("OK")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun ThemeSetting(
    selectedTheme: ThemeConfig,
    onThemeSelected: (ThemeConfig) -> Unit
) {
    Column {
        ListItem(
            headlineContent = { Text("Theme") },
            leadingContent = { Icon(Icons.Rounded.Palette, contentDescription = null) }
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ThemeConfig.entries.forEach { theme ->
                FilterChip(
                    selected = selectedTheme == theme,
                    onClick = { onThemeSelected(theme) },
                    label = { Text(theme.name.lowercase().replaceFirstChar { it.uppercase() }.replace("_", " ")) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
