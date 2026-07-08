package com.kamran.screentimetracker.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.NavDisplay
import com.kamran.screentimetracker.data.local.SettingsManager
import com.kamran.screentimetracker.data.repository.ScreenTimeRepository
import com.kamran.screentimetracker.ui.dashboard.DashboardScreen
import com.kamran.screentimetracker.ui.dashboard.DashboardViewModel
import com.kamran.screentimetracker.ui.stats.StatsScreen
import com.kamran.screentimetracker.ui.stats.StatsViewModel
import com.kamran.screentimetracker.ui.settings.SettingsScreen
import com.kamran.screentimetracker.ui.settings.SettingsViewModel

@Composable
fun NavGraph(
    repository: ScreenTimeRepository,
    settingsManager: SettingsManager,
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(Destination.Dashboard)
    val currentKey = backStack.last()

    NavigationSuiteScaffold(
        modifier = modifier,
        navigationSuiteItems = {
            item(
                selected = currentKey is Destination.Dashboard,
                onClick = { 
                    if (currentKey !is Destination.Dashboard) {
                        while (backStack.size > 1) {
                            backStack.removeAt(backStack.size - 1)
                        }
                    }
                },
                icon = { Icon(Icons.Rounded.Dashboard, contentDescription = "Dashboard") },
                label = { Text("Dashboard") }
            )
            item(
                selected = currentKey is Destination.Stats,
                onClick = {
                    if (currentKey !is Destination.Stats) {
                        backStack.add(Destination.Stats)
                    }
                },
                icon = { Icon(Icons.Rounded.BarChart, contentDescription = "Stats") },
                label = { Text("Stats") }
            )
            item(
                selected = currentKey is Destination.Settings,
                onClick = {
                    if (currentKey !is Destination.Settings) {
                        backStack.add(Destination.Settings)
                    }
                },
                icon = { Icon(Icons.Rounded.Settings, contentDescription = "Settings") },
                label = { Text("Settings") }
            )
        }
    ) {
        val entries = rememberDecoratedNavEntries<NavKey>(
            backStack = backStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = { key ->
                when (key) {
                    is Destination.Dashboard -> NavEntry(key) {
                        val viewModel: DashboardViewModel = viewModel { 
                            DashboardViewModel(repository, settingsManager) 
                        }
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToStats = { backStack.add(Destination.Stats) },
                            onNavigateToSettings = { backStack.add(Destination.Settings) }
                        )
                    }
                    is Destination.Stats -> NavEntry(key) {
                        val viewModel: StatsViewModel = viewModel { StatsViewModel(repository) }
                        StatsScreen(
                            viewModel = viewModel,
                            onNavigateBack = { backStack.removeAt(backStack.size - 1) }
                        )
                    }
                    is Destination.Settings -> NavEntry(key) {
                        val viewModel: SettingsViewModel = viewModel { 
                            SettingsViewModel(settingsManager, repository) 
                        }
                        SettingsScreen(
                            viewModel = viewModel,
                            onNavigateBack = { backStack.removeAt(backStack.size - 1) }
                        )
                    }
                    else -> NavEntry(key as NavKey) { }
                }
            }
        )

        NavDisplay(
            entries = entries,
            modifier = Modifier.fillMaxSize(),
            onBack = { 
                if (backStack.size > 1) {
                    backStack.removeAt(backStack.size - 1)
                }
            }
        )
    }
}
