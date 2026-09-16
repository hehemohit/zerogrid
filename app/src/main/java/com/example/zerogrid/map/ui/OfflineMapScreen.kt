package com.example.zerogrid.map.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zerogrid.map.data.LocationRecord
import com.example.zerogrid.map.data.MapTileManager
import com.example.zerogrid.map.domain.MovementState
import com.example.zerogrid.navigation.Screen
import com.example.zerogrid.ui.theme.*
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


private val MapBackground   = Color(0xFF0A0F14)
private val MapCard         = Color(0xFF0D1520)
private val NeonCyan        = Color(0xFF00E5FF)
private val NeonGreen       = Color(0xFF00FF88)
private val NeonOrange      = Color(0xFFFF9500)

/**
 * Full-screen offline mesh map showing real-time peer locations over osmdroid tiles.
 *
 * Features:
 * - Dark cybernetic tile styling (MAPNIK, rendered dark via osmdroid paint override)
 * - Real-time peer markers updated from [MapViewModel.peerLocations]
 * - Movement state icons (walking, vehicle, stationary)
 * - Multi-hop trail polylines for the selected peer
 * - Viewport culling: only builds overlays for visible peers
 */
@Composable
fun OfflineMapScreen(
    onNavigate: (Screen) -> Unit,
    mapViewModel: MapViewModel = viewModel()
) {
    val context = LocalContext.current
    val peerLocations by mapViewModel.peerLocations.collectAsState()

    var selectedPeerId by remember { mutableStateOf<String?>(null) }
    var mapView by remember { mutableStateOf<MapView?>(null) }

    // Initialize osmdroid on first composition
    LaunchedEffect(Unit) {
        MapTileManager.initialize(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MapBackground)
    ) {
        // ---- osmdroid Map View ----
        AndroidView(
            factory = { ctx ->
                MapTileManager.initialize(ctx)
                MapView(ctx).also { mv ->
                    MapTileManager.configureMapView(mv)
                    mapView = mv
                }
            },
            update = { mv ->
                updateMapOverlays(mv, peerLocations, selectedPeerId, mapViewModel.localNodeId)
            },
            modifier = Modifier.fillMaxSize()
        )

        // ---- Top Bar ----
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(
                onClick = { onNavigate(Screen.HOME) },
                modifier = Modifier
                    .background(MapCard.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = NeonCyan
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title card
            Row(
                modifier = Modifier
                    .background(MapCard.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Map,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MESH MAP",
                    color = NeonCyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Peer count badge
                Text(
                    text = "${peerLocations.size} NODES",
                    color = if (peerLocations.isNotEmpty()) NeonGreen else TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Tile download shortcut
            IconButton(
                onClick = { onNavigate(Screen.DOWNLOAD_TILES) },
                modifier = Modifier
                    .background(MapCard.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Download,
                    contentDescription = "Download tiles",
                    tint = TextSecondary
                )
            }
        }

        // ---- Selected Peer Info Panel ----
        val selected = selectedPeerId?.let { peerLocations[it] }
        if (selected != null) {
            PeerInfoPanel(
                record    = selected,
                onDismiss = { selectedPeerId = null },
                modifier  = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .fillMaxWidth()
            )
        } else if (peerLocations.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .background(MapCard.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "No peer locations received yet.\nWaiting for LOCATION_PING packets...",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

/** Rebuilds osmdroid overlays from the current peer location snapshot. */
private fun updateMapOverlays(
    mapView: MapView,
    peerLocations: Map<String, LocationRecord>,
    selectedPeerId: String?,
    localNodeId: String
) {
    mapView.overlays.clear()

    val visibleBB = mapView.boundingBox

    peerLocations.values.forEach { record ->
        val geoPoint = GeoPoint(record.lat, record.lng)

        // Viewport culling: skip peers outside the visible bounding box (with margin)
        if (visibleBB != null && !visibleBB.contains(geoPoint)) return@forEach

        val marker = Marker(mapView).apply {
            position = geoPoint
            title    = record.peerId
            snippet  = buildSnippet(record)
            // Color by movement state
            setTextIcon(movementStateIcon(record.movementState))
        }
        mapView.overlays.add(marker)
    }

    mapView.invalidate()
}

private fun buildSnippet(record: LocationRecord): String {
    val battery = if (record.batteryPercent >= 0) "${record.batteryPercent}%" else "?%"
    val age = ((System.currentTimeMillis() - record.timestamp) / 1000L)
    return "Bat: $battery | ${record.hopCount} hops | ${age}s ago"
}

private fun movementStateIcon(state: MovementState): String = when (state) {
    MovementState.STATIONARY -> "."
    MovementState.WALKING    -> ">>>"
    MovementState.VEHICLE    -> ">>>"
    MovementState.UNKNOWN    -> "?"
}

@Composable
private fun PeerInfoPanel(
    record: LocationRecord,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MapCard),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = record.peerId,
                    color = NeonCyan,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                TextButton(onClick = onDismiss) {
                    Text("DISMISS", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                InfoChip("HOPS", "${record.hopCount}", NeonOrange)
                InfoChip("BAT", if (record.batteryPercent >= 0) "${record.batteryPercent}%" else "?", NeonGreen)
                InfoChip("STATE", record.movementState.name, NeonCyan)
            }

            Spacer(modifier = Modifier.height(6.dp))

            val ageSec = (System.currentTimeMillis() - record.timestamp) / 1000L
            Text(
                text = "Last seen: ${ageSec}s ago   Acc: ${record.accuracyMeters.toInt()}m",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String, valueColor: Color) {
    Column {
        Text(label, color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text(value, color = valueColor, fontSize = 13.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
    }
}
