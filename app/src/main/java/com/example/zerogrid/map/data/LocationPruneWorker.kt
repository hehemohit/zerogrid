package com.example.zerogrid.map.data

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.zerogrid.mesh.engine.MeshEngine
import java.util.concurrent.TimeUnit

/**
 * WorkManager periodic task that prunes location history older than 24 hours
 * from the encrypted [ZeroGridDatabase], keeping storage bounded over long deployments.
 */
class LocationPruneWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    companion object {
        private const val TAG = "LocationPruneWorker"
        private const val WORK_NAME = "location_prune_24h"
        private const val RETENTION_MS = 24 * 60 * 60 * 1_000L // 24 hours

        /** Enqueues a recurring 24-hour prune task (idempotent — safe to call on every launch). */
        fun enqueue(context: Context) {
            val request = PeriodicWorkRequestBuilder<LocationPruneWorker>(24, TimeUnit.HOURS)
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Log.d(TAG, "Location prune worker enqueued")
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val passphrase = MeshEngine.getInstance(applicationContext).localNodeId
            val db = ZeroGridDatabase.getInstance(applicationContext, passphrase)
            val cutoff = System.currentTimeMillis() - RETENTION_MS
            val deleted = db.locationHistoryDao().pruneOlderThan(cutoff)
            Log.i(TAG, "Pruned $deleted location records older than 24h")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Prune task failed", e)
            Result.retry()
        }
    }
}
