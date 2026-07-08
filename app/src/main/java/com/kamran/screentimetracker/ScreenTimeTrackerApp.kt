package com.kamran.screentimetracker

import android.app.Application
import com.kamran.screentimetracker.worker.SyncUsageWorker

class ScreenTimeTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SyncUsageWorker.schedule(this)
    }
}
