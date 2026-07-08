package com.kamran.screentimetracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "screen_usage_events")
data class ScreenUsageEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val eventType: Int,
    val packageName: String? = null
)
