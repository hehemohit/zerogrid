package com.example.zerogrid.map.data

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import java.io.File

/**
 * Manages osmdroid offline map tile configuration, storage, and region download setup.
 * Call [initialize] once (e.g. from Application or MainActivity) before using any MapView.
 */
object MapTileManager {

    private const val TAG = "MapTileManager"

    // Zoom levels used for offline tile downloads
    const val MIN_ZOOM = 10.0
    const val MAX_ZOOM = 17.0
    const val DEFAULT_ZOOM = 13.0

    /**
     * Initializes osmdroid configuration: user agent, cache directory, and tile source.
     * Must be called before creating any [MapView].
     */
    fun initialize(context: Context) {
        Configuration.getInstance().apply {
            load(context, PreferenceManager.getDefaultSharedPreferences(context))
            userAgentValue = "ZeroGrid/${context.packageName}"
            // Set tile cache to app-private storage (survives reinstall, no external storage needed)
            osmdroidTileCache = File(context.filesDir, "osmdroid_tiles")
        }
        Log.d(TAG, "osmdroid initialized. Cache: ${Configuration.getInstance().osmdroidTileCache}")
    }

    /**
     * Configures a [MapView] with ZeroGrid's dark cybernetic styling.
     *
     * @param mapView   The map view to configure.
     * @param latitude  Initial center latitude.
     * @param longitude Initial center longitude.
     * @param zoom      Initial zoom level.
     */
    fun configureMapView(
        mapView: MapView,
        latitude: Double = 20.5937,
        longitude: Double = 78.9629,
        zoom: Double = DEFAULT_ZOOM
    ) {
        mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            minZoomLevel = MIN_ZOOM
            maxZoomLevel = MAX_ZOOM
            controller.setZoom(zoom)
            controller.setCenter(org.osmdroid.util.GeoPoint(latitude, longitude))
        }
    }

    /**
     * Estimates the number of tiles required to cover a bounding box at given zoom levels.
     * Used by [TileRegionDownloadScreen] to calculate storage requirements.
     *
     * @return Estimated tile count.
     */
    fun estimateTileCount(boundingBox: BoundingBox, minZoom: Int, maxZoom: Int): Long {
        var total = 0L
        for (zoom in minZoom..maxZoom) {
            val latTiles = Math.pow(2.0, zoom.toDouble()).toInt()
            val lngTiles = Math.pow(2.0, zoom.toDouble()).toInt()
            val northTile = latToTile(boundingBox.latNorth, zoom)
            val southTile = latToTile(boundingBox.latSouth, zoom)
            val westTile = lngToTile(boundingBox.lonWest, zoom)
            val eastTile = lngToTile(boundingBox.lonEast, zoom)
            val xCount = (eastTile - westTile + 1).coerceAtLeast(1)
            val yCount = (southTile - northTile + 1).coerceAtLeast(1)
            total += xCount.toLong() * yCount.toLong()
        }
        return total
    }

    private fun latToTile(lat: Double, zoom: Int): Int {
        val n = Math.pow(2.0, zoom.toDouble())
        return ((1.0 - Math.log(Math.tan(Math.toRadians(lat)) + 1.0 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2.0 * n).toInt()
    }

    private fun lngToTile(lng: Double, zoom: Int): Int {
        return ((lng + 180.0) / 360.0 * Math.pow(2.0, zoom.toDouble())).toInt()
    }
}
