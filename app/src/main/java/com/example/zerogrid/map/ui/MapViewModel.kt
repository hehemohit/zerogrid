package com.example.zerogrid.map.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerogrid.map.data.LocationRecord
import com.example.zerogrid.mesh.engine.MeshEngine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

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
}
