package com.example.zerogrid.mesh

import androidx.compose.material.icons.automirrored.outlined.AltRoute
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zerogrid.mesh.engine.MeshEngine
import com.example.zerogrid.mesh.engine.MeshNode
import com.example.zerogrid.navigation.Screen
import com.example.zerogrid.navigation.ZeroGridBottomBar
import com.example.zerogrid.ui.theme.*

@Composable
fun NearbyDevicesScreen(onNavigate: (Screen) -> Unit = {}) {
    val context = LocalContext.current
    val meshEngine = remember { MeshEngine.getInstance(context) }
    val connectedPeers by meshEngine.connectedPeers.collectAsState()
    val isMeshActive by meshEngine.isMeshActive.collectAsState()

    var selectedFilter by remember { mutableStateOf("All") }

    val filteredPeers = remember(connectedPeers, selectedFilter) {
        when (selectedFilter) {
            "Direct" -> connectedPeers.filter { it.isDirectNeighbor }
            "2 Hops" -> connectedPeers.filter { it.hopDistance == 2 }
            "Relay" -> connectedPeers.filter { !it.isDirectNeighbor }
            else -> connectedPeers
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = { NearbyTopBar(onBackClick = { onNavigate(Screen.HOME) }) },
        bottomBar = { ZeroGridBottomBar(currentScreen = Screen.MESH, onNavigate = onNavigate) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            MeshDiscoveryCard(
                peersCount = connectedPeers.size,
                isMeshActive = isMeshActive,
                protocol = if (meshEngine.getWifiDirectRunning()) "BLE + Wi-Fi Direct" else "BLE"
            )
            Spacer(modifier = Modifier.height(16.dp))
            RadarGraphicCard(isMeshActive = isMeshActive)
            Spacer(modifier = Modifier.height(16.dp))
            FilterChipsRow(selected = selectedFilter, onSelected = { selectedFilter = it })
            Spacer(modifier = Modifier.height(16.dp))
            DevicesListSection(
                peers = filteredPeers,
                onNavigate = onNavigate
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NearbyTopBar(onBackClick: () -> Unit = {}) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = StatusActive,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Nearby Devices",
                    color = StatusActive,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
    }
}

@Composable
private fun MeshDiscoveryCard(
    peersCount: Int,
    isMeshActive: Boolean,
    protocol: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(if (isMeshActive) StatusActive else TextSecondary, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isMeshActive) "Mesh Discovery Active" else "Discovery Inactive",
                    color = if (isMeshActive) StatusActive else TextSecondary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = if (isMeshActive) "Scanning for nearby ZeroGrid devices..." else "Turn on mesh discovery in Settings",
                color = TextSecondary,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "DEVICES FOUND", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = peersCount.toString(), color = StatusActive, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "PROTOCOL", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = protocol, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RadarGraphicCard(isMeshActive: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                drawCircle(color = SurfaceDarker, radius = 50.dp.toPx(), center = center)
                drawCircle(color = DividerColor, radius = 75.dp.toPx(), center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f))
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(SurfaceDarker, CircleShape)
                    .border(1.dp, if (isMeshActive) StatusActive else TextSecondary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = "Device",
                    tint = if (isMeshActive) StatusActive else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun FilterChipsRow(selected: String, onSelected: (String) -> Unit) {
    val filters = listOf("All", "Direct", "2 Hops", "Relay")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = filter == selected
            Button(
                onClick = { onSelected(filter) },
                modifier = Modifier
                    .height(36.dp)
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) StatusActive else CardBackground,
                    contentColor = if (isSelected) Color.Black else TextSecondary
                ),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = filter,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun DevicesListSection(
    peers: List<MeshNode>,
    onNavigate: (Screen) -> Unit
) {
    if (peers.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeviceUnknown,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No devices found",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ensure Bluetooth and Wi-Fi Direct are enabled on nearby devices running ZeroGrid.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            peers.forEach { peer ->
                val statusText = if (peer.isDirectNeighbor) "Direct • Strong" else "Hop count: ${peer.hopDistance} • Mesh Relay"
                val signalBars = when {
                    peer.rssi > -60 -> 4
                    peer.rssi > -75 -> 3
                    peer.rssi > -90 -> 2
                    else -> 1
                }

                DeviceCard(
                    icon = if (peer.isDirectNeighbor) Icons.Outlined.CellTower else Icons.AutoMirrored.Outlined.AltRoute,
                    name = peer.alias.ifEmpty { peer.nodeId },
                    status = statusText,
                    subStatus = "Transport: ${peer.transportType}",
                    signalBars = signalBars,
                    actionText = "Message",
                    isActionOutlined = false,
                    onActionClick = { onNavigate(Screen.CHAT_DETAIL) }
                )
            }
        }
    }
}

@Composable
private fun DeviceCard(
    icon: ImageVector,
    name: String,
    status: String,
    subStatus: String? = null,
    signalBars: Int? = null,
    actionText: String,
    isActionOutlined: Boolean,
    onActionClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = StatusActive,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = status, color = TextSecondary, fontSize = 13.sp, fontFamily = FontFamily.Monospace)

                    if (signalBars != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            for (i in 1..4) {
                                Box(
                                    modifier = Modifier
                                        .width(10.dp)
                                        .height(4.dp)
                                        .background(
                                            if (i <= signalBars) StatusActive else SurfaceDarker,
                                            RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                        }
                    }

                    if (subStatus != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = subStatus, color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isActionOutlined) {
                OutlinedButton(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(text = actionText, fontSize = 13.sp)
                }
            } else {
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TextPrimary, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(text = actionText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ZeroGridNearbyDevicesScreen() = NearbyDevicesScreen()

@Preview(showBackground = true)
@Composable
fun ZeroGridNearbyDevicesPreview() {
    ZeroGridTheme {
        ZeroGridNearbyDevicesScreen()
    }
}