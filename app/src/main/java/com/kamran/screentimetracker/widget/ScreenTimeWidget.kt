package com.kamran.screentimetracker.widget

import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.*
import androidx.glance.text.*
import com.kamran.screentimetracker.MainActivity
import com.kamran.screentimetracker.R
import com.kamran.screentimetracker.data.local.AppDatabase
import com.kamran.screentimetracker.util.UsageStatsCalculator
import kotlinx.coroutines.flow.first

class ScreenTimeWidget : GlanceAppWidget() {
    
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = AppDatabase.getDatabase(context)
        val events = database.screenEventDao().getAllEvents().first()
        val todayMillis = UsageStatsCalculator.calculateTodayStats(events)
        
        val hours = todayMillis / (1000 * 60 * 60)
        val minutes = (todayMillis / (1000 * 60)) % 60

        provideContent {
            val size = LocalSize.current
            val ctx = LocalContext.current
            GlanceTheme {
                WidgetLayout(ctx, hours, minutes, size)
            }
        }
    }

    @Composable
    private fun WidgetLayout(context: Context, hours: Long, minutes: Long, size: DpSize) {
        val isSmall = size.height < 100.dp
        
        // Responsive font sizes
        val titleFontSize = if (isSmall) 11.sp else 14.sp
        val timeFontSize = if (isSmall) 28.sp else 42.sp
        val unitFontSize = if (isSmall) 12.sp else 18.sp

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.primaryContainer)
                .padding(8.dp)
                .clickable(actionStartActivity(ComponentName(context, MainActivity::class.java)))
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                verticalAlignment = Alignment.Vertical.CenterVertically
            ) {
                Text(
                    text = "Today's Usage",
                    style = TextStyle(
                        color = GlanceTheme.colors.onPrimaryContainer,
                        fontSize = titleFontSize,
                        fontWeight = FontWeight.Medium
                    )
                )
                
                Row(verticalAlignment = Alignment.Vertical.Bottom) {
                    Text(
                        text = hours.toString(),
                        style = TextStyle(
                            color = GlanceTheme.colors.onPrimaryContainer,
                            fontSize = timeFontSize,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "h",
                        style = TextStyle(
                            color = GlanceTheme.colors.onPrimaryContainer,
                            fontSize = unitFontSize
                        ),
                        modifier = GlanceModifier.padding(bottom = (timeFontSize.value / 8).dp, start = 2.dp)
                    )
                    Spacer(modifier = GlanceModifier.width(if (isSmall) 4.dp else 8.dp))
                    Text(
                        text = minutes.toString(),
                        style = TextStyle(
                            color = GlanceTheme.colors.onPrimaryContainer,
                            fontSize = timeFontSize,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "m",
                        style = TextStyle(
                            color = GlanceTheme.colors.onPrimaryContainer,
                            fontSize = unitFontSize
                        ),
                        modifier = GlanceModifier.padding(bottom = (timeFontSize.value / 8).dp, start = 2.dp)
                    )
                }
            }

            // Refresh Button in Top Right
            Box(
                modifier = GlanceModifier.fillMaxSize(),
                contentAlignment = Alignment.TopEnd
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_refresh),
                    contentDescription = "Refresh",
                    modifier = GlanceModifier
                        .size(if (isSmall) 20.dp else 24.dp)
                        .padding(4.dp)
                        .clickable(actionRunCallback<RefreshAction>())
                )
            }
        }
    }
}
