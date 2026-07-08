package com.kamran.screentimetracker.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kamran.screentimetracker.data.local.entity.ScreenUsageEvent
import com.kamran.screentimetracker.data.repository.ScreenTimeRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar

data class DailyUsage(
    val dayLabel: String,
    val timeMillis: Long
)

data class StatsUiState(
    val weeklyUsage: List<DailyUsage> = emptyList()
)

class StatsViewModel(
    private val repository: ScreenTimeRepository
) : ViewModel() {

    val uiState: StateFlow<StatsUiState> = repository.getAllEvents()
        .map { events ->
            calculateWeeklyStats(events)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StatsUiState()
        )

    private fun calculateWeeklyStats(events: List<ScreenUsageEvent>): StatsUiState {
        val calendar = Calendar.getInstance()
        val dailyUsageList = mutableListOf<DailyUsage>()

        for (i in 0 until 7) {
            val startOfDay = calendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val endOfDay = calendar.apply {
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            val totalTime = com.kamran.screentimetracker.util.UsageStatsCalculator.calculateDayStats(
                events = events,
                startOfDay = startOfDay,
                endOfDay = endOfDay,
                isToday = i == 0
            )

            val dayLabel = when(i) {
                0 -> "Today"
                1 -> "Yesterday"
                else -> {
                    val days = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
                    days[calendar.get(Calendar.DAY_OF_WEEK) - 1]
                }
            }

            dailyUsageList.add(DailyUsage(dayLabel, totalTime))
            calendar.add(Calendar.DAY_OF_YEAR, -1)
        }

        return StatsUiState(weeklyUsage = dailyUsageList.reversed())
    }
}
