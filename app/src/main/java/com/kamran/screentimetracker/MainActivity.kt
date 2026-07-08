package com.kamran.screentimetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.kamran.screentimetracker.data.local.AppDatabase
import com.kamran.screentimetracker.data.local.SettingsManager
import com.kamran.screentimetracker.data.local.ThemeConfig
import com.kamran.screentimetracker.data.repository.ScreenTimeRepository
import com.kamran.screentimetracker.ui.navigation.NavGraph
import com.kamran.screentimetracker.ui.theme.ScreenTimeTrackerTheme
import com.kamran.screentimetracker.util.PermissionUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ScreenTimeRepository(applicationContext, database.screenEventDao())
        val settingsManager = SettingsManager(applicationContext)

        setContent {
            val themeConfig by settingsManager.themeConfig.collectAsState(ThemeConfig.FOLLOW_SYSTEM)
            val darkTheme = when (themeConfig) {
                ThemeConfig.LIGHT -> false
                ThemeConfig.DARK -> true
                ThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
            }

            ScreenTimeTrackerTheme(darkTheme = darkTheme) {
                MainScreen(repository, settingsManager)
            }
        }
    }
}

@Composable
fun MainScreen(repository: ScreenTimeRepository, settingsManager: SettingsManager) {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(PermissionUtils.hasUsageStatsPermission(context)) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasPermission = PermissionUtils.hasUsageStatsPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (!hasPermission) {
            PermissionRequestScreen(
                onGrantClick = { PermissionUtils.openUsageStatsSettings(context) }
            )
        } else {
            NavGraph(repository = repository, settingsManager = settingsManager)
        }
    }
}

@Composable
fun PermissionRequestScreen(onGrantClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Usage Stats Permission Required",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "This app needs access to your usage statistics to track screen time and unlocks. Please grant the permission in the settings.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onGrantClick) {
            Text("Grant Permission")
        }
    }
}
