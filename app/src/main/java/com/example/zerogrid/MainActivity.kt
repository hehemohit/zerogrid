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

@Composable
fun MainAppGateway() {
    val context = LocalContext.current
    val sessionManager = remember { UserSessionManager.getInstance(context) }

    var currentRoute by remember { mutableStateOf(AppRoute.ROUTE_ROLE_SELECTION) }
    var currentUserName by remember { mutableStateOf(sessionManager.getUserName()) }
    var showFullMeshApp by remember { mutableStateOf(false) }

    // Hardware and gesture back handling
    BackHandler(enabled = (currentRoute != AppRoute.ROUTE_ROLE_SELECTION || showFullMeshApp)) {
        if (showFullMeshApp) {
            showFullMeshApp = false
        } else {
            currentRoute = AppRoute.ROUTE_ROLE_SELECTION
        }
    }

    if (showFullMeshApp) {
        ZeroGridApp()
    } else {
        when (currentRoute) {
            AppRoute.ROUTE_ROLE_SELECTION -> {
                RoleSelectionScreen(
                    onRoleSelected = { role, name ->
                        currentUserName = name
                        currentRoute = when (role) {
                            UserRole.CITIZEN -> AppRoute.ROUTE_USER_DASHBOARD
                            UserRole.AUTHORITY -> AppRoute.ROUTE_AUTHORITY_DASHBOARD
                        }
                    }
                )
            }
            AppRoute.ROUTE_USER_DASHBOARD -> {
                UserDashboardScreen(
                    userName = currentUserName,
                    onBackToRoles = {
                        currentRoute = AppRoute.ROUTE_ROLE_SELECTION
                    },
                    onOpenFullMeshApp = {
                        showFullMeshApp = true
                    }
                )
            }
            AppRoute.ROUTE_AUTHORITY_DASHBOARD -> {
                AuthorityDashboardScreen(
                    userName = currentUserName,
                    onBackToRoles = {
                        currentRoute = AppRoute.ROUTE_ROLE_SELECTION
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
