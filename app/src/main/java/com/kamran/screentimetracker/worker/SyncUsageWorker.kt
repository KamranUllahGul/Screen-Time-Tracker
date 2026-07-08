package com.kamran.screentimetracker.worker

import android.content.Context
import androidx.work.*
import com.kamran.screentimetracker.data.local.AppDatabase
import com.kamran.screentimetracker.data.repository.ScreenTimeRepository
import com.kamran.screentimetracker.widget.WidgetUpdater
import java.util.concurrent.TimeUnit

class SyncUsageWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = ScreenTimeRepository(applicationContext, database.screenEventDao())

        return try {
            repository.syncUsageEvents()
            WidgetUpdater.update(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "sync_usage_work"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<SyncUsageWorker>(
                15, TimeUnit.MINUTES
            )
            .setConstraints(constraints)
            .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
