package com.example.zerogrid.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.zerogrid.MainActivity
import com.example.zerogrid.mesh.engine.MeshEngine

/**
 * Android Foreground Service holding the Mesh Engine active in the background.
 */
class MeshForegroundService : Service() {

    companion object {
        private const val TAG = "MeshForegroundService"
        private const val CHANNEL_ID = "zerogrid_mesh_channel"
        private const val NOTIFICATION_ID = 1001

        fun startService(context: Context) {
            try {
                val intent = Intent(context, MeshForegroundService::class.java)
                context.startForegroundService(intent)
            } catch (e: Throwable) {
                Log.e(TAG, "Error starting MeshForegroundService", e)
            }
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, MeshForegroundService::class.java)
                context.stopService(intent)
            } catch (e: Throwable) {
                Log.e(TAG, "Error stopping MeshForegroundService", e)
            }
        }
    }

    private var meshEngine: MeshEngine? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            meshEngine = MeshEngine.getInstance(applicationContext)
            val nodeIdText = meshEngine?.localNodeId ?: "ZeroGrid"
            val notification = createNotification("Node ID: $nodeIdText • Mesh Active")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE,
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }

            meshEngine?.startMesh()
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize MeshForegroundService on start", e)
        }
        return START_STICKY
    }

    override fun onDestroy() {
        try {
            meshEngine?.stopMesh()
        } catch (e: Throwable) {
            Log.e(TAG, "Error stopping mesh engine", e)
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(statusText: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("ZeroGrid Mesh Active")
            .setContentText(statusText)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "ZeroGrid Mesh Daemon",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Keeps BLE & Wi-Fi Direct multi-hop routing active"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }
}
