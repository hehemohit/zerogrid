package com.zerogrid.mesh.app

import android.app.Application
import android.util.Log
import com.zerogrid.mesh.app.map.data.MapTileManager

/**
 * Application entry point for ZeroGrid.
 *
 * Ensures global, lifecycle-independent initialization of critical components
 * (such as osmdroid configuration, tile cache directories, and User-Agent headers)
 * before any Activity, Service, or WorkManager worker is launched.
 */
class ZeroGridApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d("ZeroGridApplication", "Initializing ZeroGridApplication...")
        MapTileManager.initialize(this)
    }
}
