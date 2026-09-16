package com.zerogrid.mesh.app.map.domain

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.zerogrid.mesh.app.map.data.LocationPacket
import com.zerogrid.mesh.app.map.data.LocationPacketCodec
import com.zerogrid.mesh.app.mesh.engine.MeshEngine
import com.zerogrid.mesh.app.mesh.engine.MeshPacket
import com.zerogrid.mesh.app.mesh.engine.PacketType
import kotlinx.coroutines.flow.StateFlow

/**
 * Manages GPS acquisition, movement-state-driven adaptive sampling, rate limiting,
 * and location broadcast over the ZeroGrid mesh.
 *
 * Integrates:
 * - [android.location.LocationManager] for GPS fixes (no Google Play Services).
 * - [MovementClassifier] for on-device state detection.
 * - [SignificantMovementDetector] for significance filtering.
 * - [BroadcastRateLimiter] for mesh congestion prevention.
 * - [MeshEngine] for packet transmission.
 */
class LocationBroadcastManager(
    private val context: Context,
    private val meshEngine: MeshEngine,
    private val movementState: StateFlow<MovementState>
) {
    companion object {
        private const val TAG = "LocationBroadcastManager"
    }

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    private val detector = SignificantMovementDetector()
    private val rateLimiter = BroadcastRateLimiter()

    private var isRunning = false

    private val locationListener = LocationListener { location ->
        onLocationUpdate(location)
    }

    private val gpsProviderReceiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            if (intent.action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                val gpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
                Log.i(TAG, "GPS provider changed — enabled: $gpsEnabled")
            }
        }
    }

    /** Starts GPS acquisition and begins the adaptive broadcast loop. */
    @SuppressLint("MissingPermission")
    fun start() {
        if (isRunning) return
        isRunning = true

        // Register GPS provider change receiver
        val filter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(gpsProviderReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(gpsProviderReceiver, filter)
        }

        // Request location updates — use the most liberal config upfront;
        // the SignificantMovementDetector gates actual broadcasts.
        try {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5_000L,   // 5 s minimum
                5f,       // 5 m minimum
                locationListener,
                Looper.getMainLooper()
            )
            Log.d(TAG, "GPS location updates requested")
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission denied", e)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to request location updates", e)
        }
    }

    /** Stops GPS acquisition and cleans up resources. */
    fun stop() {
        if (!isRunning) return
        isRunning = false
        try {
            locationManager?.removeUpdates(locationListener)
            context.unregisterReceiver(gpsProviderReceiver)
        } catch (e: Exception) {
            Log.w(TAG, "Error during stop: ${e.message}")
        }
        rateLimiter.reset()
        detector.reset()
        Log.d(TAG, "LocationBroadcastManager stopped")
    }

    private fun onLocationUpdate(location: Location) {
        val state = movementState.value
        val significant = detector.evaluate(location, state) ?: return

        if (!rateLimiter.tryAcquire()) return

        val peerIdHash = LocationPacketCodec.computePeerIdHash(meshEngine.localNodeId)
        val packet = LocationPacket(
            peerId        = meshEngine.localNodeId,
            peerIdHash    = peerIdHash,
            lat           = significant.latitude,
            lng           = significant.longitude,
            accuracyMeters = significant.accuracy,
            timestamp     = significant.time,
            batteryPercent = getBatteryLevel(),
            ttl           = LocationPacket.LOCATION_DEFAULT_TTL,
            movementState  = state
        )

        val encoded = LocationPacketCodec.encode(packet) ?: run {
            Log.e(TAG, "Failed to encode location packet")
            return
        }

        val meshPacket = MeshPacket(
            senderId    = meshEngine.localNodeId,
            recipientId = MeshPacket.BROADCAST_ADDRESS,
            ttl         = LocationPacket.LOCATION_DEFAULT_TTL,
            type        = PacketType.LOCATION_PING,
            payload     = android.util.Base64.encodeToString(encoded, android.util.Base64.NO_WRAP)
        )

        meshEngine.broadcastLocationPacket(meshPacket)
        Log.d(TAG, "Broadcast location: lat=${significant.latitude}, lng=${significant.longitude}, state=$state")
    }

    @SuppressLint("PrivateApi")
    private fun getBatteryLevel(): Int {
        return try {
            val intent = context.registerReceiver(null,
                IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = intent?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) ((level.toFloat() / scale) * 100).toInt() else -1
        } catch (e: Exception) { -1 }
    }
}
