package com.example.zerogrid

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.zerogrid.navigation.ZeroGridApp
import com.example.zerogrid.service.MeshForegroundService
import com.example.zerogrid.ui.theme.ZeroGridTheme
import com.zerogrid.mesh.app.ui.*
import com.zerogrid.mesh.app.ui.navigation.AppScreen

@Composable
fun MainAppGateway() {
    val context = LocalContext.current
    val sessionManager = remember { UserSessionManager.getInstance(context) }

    var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.RoleSelection) }
    var currentUserName by remember { mutableStateOf(sessionManager.getUserName()) }
    var showFullMeshApp by remember { mutableStateOf(false) }

    // Hardware and gesture back handling
    BackHandler(enabled = (currentScreen != AppScreen.RoleSelection || showFullMeshApp)) {
        if (showFullMeshApp) {
            showFullMeshApp = false
        } else {
            when (currentScreen) {
                is AppScreen.NameEntry -> currentScreen = AppScreen.RoleSelection
                AppScreen.UserDashboard -> currentScreen = AppScreen.RoleSelection
                AppScreen.AuthorityDashboard -> currentScreen = AppScreen.RoleSelection
                AppScreen.RoleSelection -> { /* At root */ }
            }
        }
    }

    if (showFullMeshApp) {
        ZeroGridApp()
    } else {
        when (val screen = currentScreen) {
            AppScreen.RoleSelection -> {
                RoleSelectionScreen(
                    onSelectRole = { isAuthority ->
                        currentScreen = AppScreen.NameEntry(isAuthority = isAuthority)
                    }
                )
            }
            is AppScreen.NameEntry -> {
                NameEntryScreen(
                    isAuthority = screen.isAuthority,
                    onNameConfirmed = { name ->
                        currentUserName = name
                        currentScreen = if (screen.isAuthority) {
                            AppScreen.AuthorityDashboard
                        } else {
                            AppScreen.UserDashboard
                        }
                    },
                    onBack = {
                        currentScreen = AppScreen.RoleSelection
                    }
                )
            }
            AppScreen.UserDashboard -> {
                UserDashboardScreen(
                    userName = currentUserName,
                    onBackToRoles = {
                        currentScreen = AppScreen.RoleSelection
                    },
                    onOpenFullMeshApp = {
                        showFullMeshApp = true
                    }
                )
            }
            AppScreen.AuthorityDashboard -> {
                AuthorityDashboardScreen(
                    userName = currentUserName,
                    onBackToRoles = {
                        currentScreen = AppScreen.RoleSelection
                    },
                    onOpenFullMeshApp = {
                        showFullMeshApp = true
                    }
                )
            }
        }
    }
}

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            Log.d("MainActivity", "All required permissions granted. Starting mesh service.")
            MeshForegroundService.startService(this)
        } else {
            Log.w("MainActivity", "Some permissions were denied. Mesh functionality may be limited.")
            // Still start service, drivers will handle missing permissions gracefully
            MeshForegroundService.startService(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val startTime = System.currentTimeMillis()
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate started")

        checkAndRequestPermissions()

        setContent {
            ZeroGridTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppGateway()
                }
            }
        }
        Log.d("MainActivity", "onCreate finished in ${System.currentTimeMillis() - startTime}ms")
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf<String>()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }
        
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            Log.d("MainActivity", "Requesting missing permissions: $missingPermissions")
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        } else {
            Log.d("MainActivity", "All permissions already granted. Starting mesh service.")
            MeshForegroundService.startService(this)
        }
    }
}
