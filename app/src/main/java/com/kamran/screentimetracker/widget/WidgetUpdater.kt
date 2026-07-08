package com.kamran.screentimetracker.widget

import android.content.Context
import androidx.glance.appwidget.updateAll

object WidgetUpdater {
    suspend fun update(context: Context) {
        ScreenTimeWidget().updateAll(context)
    }
}
