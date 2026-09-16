package com.zerogrid.mesh.app.map.data

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.tileprovider.tilesource.TileSourcePolicy
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView
import java.io.File

/**
 * Manages osmdroid offline map tile configuration, storage, and region download setup.
 * Call [initialize] once (e.g. from Application) before using any MapView.
 */
object MapTileManager {

    private const val TAG = "MapTileManager"
    private const val PREF_TILE_CACHE_VERSION = "zerogrid_tile_cache_version"
    private const val CURRENT_CACHE_VERSION = "v5"

    // Compliant User-Agent complying with OpenStreetMap Tile Usage Policy (identifies application and contact)
    const val USER_AGENT = "ZeroGridMesh/1.0 (Android; Offline Mesh Communication; contact@zerogrid-project.org)"

    // Custom Tile Source that uses flag 0 to completely bypass dynamic package inspection
    // and user agent normalization, ensuring the static userAgentValue is used directly without getPackageName() calls
    val ZERO_GRID_TILE_SOURCE: OnlineTileSourceBase = XYTileSource(
        "ZeroGridMapnik",
        0,
        19,
        256,
        ".png",
        arrayOf(
            "https://tile.openstreetmap.org/"
        ),
        "© OpenStreetMap contributors",
        TileSourcePolicy(
            2,
            0 // Bypass dynamic package inspection and normalization
        )
    )

    // Zoom levels used for offline tile downloads
    const val MIN_ZOOM = 10.0
    const val MAX_ZOOM = 17.0
    const val DEFAULT_ZOOM = 13.0

    @Volatile
    private var isInitialized = false

    /**
     * Initializes osmdroid configuration: user agent, cache directory, and tile source.
     * Guaranteed to run only once at Application startup.
     */
    fun initialize(context: Context) {
        if (isInitialized) return
        synchronized(this) {
            if (isInitialized) return
            isInitialized = true

            val appContext = context.applicationContext
            val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)

            // 1. Force set the global configuration values explicitly
            Configuration.getInstance().apply {
                load(appContext, prefs)
                userAgentValue = USER_AGENT
                userAgentHttpHeader = "User-Agent"
                osmdroidTileCache = File(appContext.cacheDir, "osmdroid_tiles")

                // Defense-in-depth: override mNormalizedUserAgent via reflection
                // so osmdroid never emits "com.zerogrid.mesh.app/1" even if standard sources are queried
                try {
                    val field = javaClass.getDeclaredField("mNormalizedUserAgent")
                    field.isAccessible = true
                    field.set(this, USER_AGENT)
                } catch (e: Throwable) {
                    Log.w(TAG, "Could not override mNormalizedUserAgent via reflection", e)
                }

                save(appContext, prefs)
            }

            // 2. FORCE osmdroid's internal tile provider constants to lock in the User-Agent
            try {
                val clazz = Class.forName("org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants")
                val method = clazz.getMethod("setUserAgentValue", String::class.java)
                method.invoke(null, USER_AGENT)
            } catch (e: Throwable) {
                Log.d(TAG, "OpenStreetMapTileProviderConstants.setUserAgentValue: ${e.message}")
            }

            // Always purge any stale/corrupted disk cache so tiles are fetched live
            clearTileCache(appContext)

            Log.d(TAG, "osmdroid initialized in live online streaming mode with User-Agent: $USER_AGENT")
        }
    }

    /**
     * Purges tile cache on disk to ensure no stale or 403 Forbidden tiles remain.
     */
    fun clearTileCache(context: Context) {
        try {
            val privateDir = File(context.filesDir, "osmdroid_tiles")
            if (privateDir.exists()) {
                privateDir.deleteRecursively()
            }
            val cacheDir = File(context.cacheDir, "osmdroid_tiles")
            if (cacheDir.exists()) {
                cacheDir.deleteRecursively()
            }
            val osmTemp = File(context.cacheDir, "osmdroid")
            if (osmTemp.exists()) {
                osmTemp.deleteRecursively()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to purge tile cache", e)
        }
    }

    /**
     * Configures a [MapView] for live online streaming directly from the OSM server.
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
            setTileSource(ZERO_GRID_TILE_SOURCE)
            setUseDataConnection(true) // Direct online viewing from OSM server
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
