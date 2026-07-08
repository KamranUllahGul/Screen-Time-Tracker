package com.kamran.screentimetracker.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.kamran.screentimetracker.data.local.AppDatabase
import com.kamran.screentimetracker.data.repository.ScreenTimeRepository

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val database = AppDatabase.getDatabase(context)
        val repository = ScreenTimeRepository(context, database.screenEventDao())
        
        // Sync usage events
        repository.syncUsageEvents()
        
        // Update the widget
        WidgetUpdater.update(context)
    }
}
