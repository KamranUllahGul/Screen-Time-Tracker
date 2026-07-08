package com.kamran.screentimetracker.util

import android.app.usage.UsageEvents
import com.kamran.screentimetracker.data.local.entity.ScreenUsageEvent
import java.util.Calendar

object UsageStatsCalculator {
    fun calculateDayStats(events: List<ScreenUsageEvent>, startOfDay: Long, endOfDay: Long, isToday: Boolean): Long {
        val dayEvents = events.filter { it.timestamp in startOfDay..endOfDay }.sortedBy { it.timestamp }
        
        var totalTime = 0L
        var lastInteractiveTime = -1L

        for (event in dayEvents) {
            when (event.eventType) {
                UsageEvents.Event.SCREEN_INTERACTIVE -> {
                    lastInteractiveTime = event.timestamp
                }
                UsageEvents.Event.SCREEN_NON_INTERACTIVE -> {
                    if (lastInteractiveTime != -1L) {
                        totalTime += (event.timestamp - lastInteractiveTime)
                        lastInteractiveTime = -1L
                    }
                }
            }
        }

        // If it's today and currently interactive
        if (isToday && lastInteractiveTime != -1L) {
            totalTime += (System.currentTimeMillis() - lastInteractiveTime)
        }

        return totalTime
    }

    fun calculateTodayStats(events: List<ScreenUsageEvent>): Long {
        val calendar = Calendar.getInstance()
        val startOfToday = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val endOfToday = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        return calculateDayStats(events, startOfToday, endOfToday, true)
    }
}
