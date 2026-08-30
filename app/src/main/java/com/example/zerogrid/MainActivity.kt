package com.example.zerogrid

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.zerogrid.navigation.ZeroGridApp
import com.example.zerogrid.service.MeshForegroundService
import com.example.zerogrid.ui.theme.ZeroGridTheme

@Composable
fun ZeroGridScreen() {
    ZeroGridApp()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start ZeroGrid Mesh Foreground Service
        // The service will handle MeshEngine initialization and startup
        try {
            MeshForegroundService.startService(this)
        } catch (e: Throwable) {
            Log.e("MainActivity", "Error starting MeshForegroundService", e)
        }

        setContent {
            ZeroGridTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ZeroGridScreen()
                }
            }
        }
    }
}
