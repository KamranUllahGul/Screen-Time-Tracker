package com.kamran.screentimetracker.data.repository

import android.annotation.SuppressLint
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import com.kamran.screentimetracker.data.local.dao.ScreenEventDao
import com.kamran.screentimetracker.data.local.entity.ScreenUsageEvent
import com.kamran.screentimetracker.util.PermissionUtils
import kotlinx.coroutines.flow.Flow

class ScreenTimeRepository(
    private val context: Context,
    private val screenEventDao: ScreenEventDao
) {
    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    @SuppressLint("MissingPermission")
    suspend fun syncUsageEvents() {
        if (!PermissionUtils.hasUsageStatsPermission(context)) return

        val lastTimestamp = screenEventDao.getLastEventTimestamp() ?: (System.currentTimeMillis() - 24 * 60 * 60 * 1000)
        val currentTimestamp = System.currentTimeMillis()

        val events = mutableListOf<ScreenUsageEvent>()
        val usageEvents = usageStatsManager.queryEvents(lastTimestamp + 1, currentTimestamp)
        val event = UsageEvents.Event()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.SCREEN_INTERACTIVE ||
                event.eventType == UsageEvents.Event.SCREEN_NON_INTERACTIVE ||
                event.eventType == UsageEvents.Event.KEYGUARD_HIDDEN ||
                event.eventType == UsageEvents.Event.KEYGUARD_SHOWN
            ) {
                events.add(
                    ScreenUsageEvent(
                        timestamp = event.timeStamp,
                        eventType = event.eventType,
                        packageName = event.packageName
                    )
                )
            }
        }

        if (events.isNotEmpty()) {
            screenEventDao.insertEvents(events)
        }
    }

    fun getAllEvents(): Flow<List<ScreenUsageEvent>> = screenEventDao.getAllEvents()

    fun getEventsInRange(startTime: Long, endTime: Long): Flow<List<ScreenUsageEvent>> =
        screenEventDao.getEventsInRange(startTime, endTime)
}
