package com.zerogrid.mesh.app.map.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zerogrid.mesh.app.map.data.LocationRecord
import com.zerogrid.mesh.app.mesh.engine.MeshEngine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for [OfflineMapScreen] and [TileRegionDownloadScreen].
 *
 * Collects peer location data from [MeshEngine.peerLocations] and provides
 * a stable, lifecycle-aware stream for the Compose UI.
 */
class MapViewModel(application: Application) : AndroidViewModel(application) {

    private val meshEngine = MeshEngine.getInstance(application)

    /**
     * Live map of all known peer locations: NodeId -> [LocationRecord].
     * Updated in real-time as LOCATION_PING packets arrive over the mesh.
     */
    val peerLocations: StateFlow<Map<String, LocationRecord>> = meshEngine.peerLocations
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap()
        )

    /** The local device's mesh node ID. */
    val localNodeId: String = meshEngine.localNodeId

    /**
     * Immediately fetches current GPS location (or best known provider fix)
     * and broadcasts a forced LOCATION_PING over the mesh, bypassing all rate-limits.
     */
    fun pingCurrentLocation(onResult: (success: Boolean, message: String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val app = getApplication<Application>()
            val locationManager = app.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager == null) {
                withContext(Dispatchers.Main) { onResult(false, "Location service not available") }
                return@launch
            }

            // Find best available cached location from enabled providers
            val loc = try {
                val providers = locationManager.getProviders(true)
                var bestLoc: Location? = null
                for (provider in providers) {
                    val candidate = locationManager.getLastKnownLocation(provider) ?: continue
                    if (bestLoc == null || candidate.time > bestLoc.time) {
                        bestLoc = candidate
                    }
                }
                bestLoc
            } catch (e: SecurityException) {
                withContext(Dispatchers.Main) { onResult(false, "Location permission missing") }
                return@launch
            } catch (e: Exception) {
                null
            }

            if (loc != null) {
                val ok = meshEngine.broadcastManualLocationPing(
                    lat = loc.latitude,
                    lng = loc.longitude,
                    accuracyMeters = loc.accuracy
                )
                withContext(Dispatchers.Main) {
                    if (ok) {
                        val latStr = String.format(java.util.Locale.US, "%.4f", loc.latitude)
                        val lngStr = String.format(java.util.Locale.US, "%.4f", loc.longitude)
                        onResult(true, "Ping sent to mesh: ($latStr, $lngStr)")
                    } else {
                        onResult(false, "Failed to encode location ping")
                    }
                }
            } else {
                // Request a single fresh update from GPS / Network
                withContext(Dispatchers.Main) {
                    try {
                        val listener = object : LocationListener {
                            override fun onLocationChanged(freshLoc: Location) {
                                locationManager.removeUpdates(this)
                                val ok = meshEngine.broadcastManualLocationPing(
                                    lat = freshLoc.latitude,
                                    lng = freshLoc.longitude,
                                    accuracyMeters = freshLoc.accuracy
                                )
                                if (ok) {
                                    val latStr = String.format(java.util.Locale.US, "%.4f", freshLoc.latitude)
                                    val lngStr = String.format(java.util.Locale.US, "%.4f", freshLoc.longitude)
                                    onResult(true, "GPS ping sent to mesh: ($latStr, $lngStr)")
                                }
                            }
                            @Deprecated("Deprecated in Java")
                            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                        }
                        val provider = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                            LocationManager.GPS_PROVIDER
                        } else LocationManager.NETWORK_PROVIDER
                        locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                        onResult(true, "Acquiring GPS fix... ping will broadcast shortly")
                    } catch (e: Exception) {
                        // Fallback test coordinates if device has no GPS fix (e.g. testing indoors/emulator)
                        val ok = meshEngine.broadcastManualLocationPing(28.6139, 77.2090, 10f)
                        if (ok) {
                            onResult(true, "Test location ping sent (28.6139, 77.2090)")
                        } else {
                            onResult(false, "No GPS fix available. Enable GPS.")
                        }
                    }
                }
            }
        }
    }
}
