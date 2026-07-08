package com.kamran.screentimetracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kamran.screentimetracker.data.local.entity.ScreenUsageEvent
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreenEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<ScreenUsageEvent>)

    @Query("SELECT * FROM screen_usage_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<ScreenUsageEvent>>

    @Query("SELECT MAX(timestamp) FROM screen_usage_events")
    suspend fun getLastEventTimestamp(): Long?

    @Query("SELECT * FROM screen_usage_events WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp ASC")
    fun getEventsInRange(startTime: Long, endTime: Long): Flow<List<ScreenUsageEvent>>

    @Query("DELETE FROM screen_usage_events")
    suspend fun clearAll()
}
