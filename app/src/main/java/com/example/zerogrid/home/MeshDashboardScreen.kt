package com.example.zerogrid.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.zerogrid.mesh.engine.MeshEngine
import com.example.zerogrid.mesh.engine.MeshNode
import com.example.zerogrid.navigation.Screen
import com.example.zerogrid.navigation.ZeroGridBottomBar
import com.example.zerogrid.ui.theme.*

@Composable
fun MeshDashboardScreen(onNavigate: (Screen) -> Unit = {}) {
    val meshEngine = MeshEngine.getInstance(LocalContext.current)
    val peers by meshEngine.connectedPeers.collectAsState()
    val isMeshActive by meshEngine.isMeshActive.collectAsState()

    Scaffold(
        containerColor = DarkBackground,
        topBar = { DashboardTopBar(isMeshActive = isMeshActive) },
        bottomBar = { ZeroGridBottomBar(currentScreen = Screen.HOME, onNavigate = onNavigate) },
        floatingActionButton = { SOSFab(onNavigate = onNavigate) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            MeshStatusCard(peersCount = peers.size, onNavigate = onNavigate)
            Spacer(modifier = Modifier.height(16.dp))
            QuickActionsGrid(peersCount = peers.size, onNavigate = onNavigate)
            Spacer(modifier = Modifier.height(24.dp))
            NearbyDevicesSection(peers = peers, onNavigate = onNavigate)
            Spacer(modifier = Modifier.height(32.dp)) // Extra space for FAB
        }
    }
}

@Composable
private fun DashboardTopBar(isMeshActive: Boolean) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = "Shield",
                    tint = if (isMeshActive) StatusActive else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ZeroGrid",
                    color = if (isMeshActive) StatusActive else TextSecondary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Mesh Active Pill
                Row(
                    modifier = Modifier
                        .background(
                            if (isMeshActive) Color(0xFF1A3B40) else SurfaceDarker, 
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(if (isMeshActive) StatusActive else TextSecondary, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isMeshActive) "Mesh Active" else "Mesh Offline",
                        color = if (isMeshActive) StatusActive else TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.SignalCellularAlt,
                    contentDescription = "Signal",
                    tint = if (isMeshActive) StatusActive else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
    }
}

@Composable
private fun MeshStatusCard(peersCount: Int, onNavigate: (Screen) -> Unit = {}) {
    Card(
        onClick = { onNavigate(Screen.NETWORK_STATUS) },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(StatusActive, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (peersCount > 0) "MESH CONNECTED" else "SCANNING FOR PEERS",
                        color = StatusActive,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "Mesh",
                    tint = StatusActive,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = "Multi-protocol Discovery Active",
                color = TextSecondary,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // Metrics Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Peers", peersCount.toString(), Modifier.weight(1f))
                MetricCard("Routes", if (peersCount > 0) "1" else "0", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Relays", "0", Modifier.weight(1f))
                MetricCard("Latency", if (peersCount > 0) "45ms" else "--", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Internet unavailable • ZeroGrid operating normally",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(SurfaceDarker, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(text = label, color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun QuickActionsGrid(peersCount: Int, onNavigate: (Screen) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.ChatBubbleOutline,
                title = "Messages",
                badgeText = null,
                iconTint = StatusActive,
                onClick = { onNavigate(Screen.MESSAGES) }
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Share,
                title = "Mesh Network",
                subtitle = "$peersCount active",
                iconTint = StatusActive,
                onClick = { onNavigate(Screen.MESH) }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Folder,
                title = "Files",
                subtitle = "2 active transfers",
                iconTint = StatusActive,
                onClick = { onNavigate(Screen.FILES) }
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Emergency,
                title = "SOS",
                subtitle = "2 active alerts",
                iconTint = AlertPink,
                borderColor = AlertRedBorder,
                subtitleColor = AlertPink,
                onClick = { onNavigate(Screen.SOS_CENTER) }
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    badgeText: String? = null,
    iconTint: Color,
    borderColor: Color = Color.Transparent,
    subtitleColor: Color = TextSecondary,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(120.dp)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (badgeText != null) {
                Text(
                    text = badgeText,
                    color = Color.Black,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 12.dp, end = 12.dp)
                        .background(StatusActive, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = title, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = subtitle, color = subtitleColor, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun NearbyDevicesSection(peers: List<MeshNode>, onNavigate: (Screen) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Nearby Devices", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = { onNavigate(Screen.MESH) }) {
                Text(text = "View All", color = StatusActive, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                if (peers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(text = "Scanning for peers...", color = TextSecondary, fontSize = 14.sp)
                    }
                } else {
                    peers.take(5).forEachIndexed { index, peer ->
                        DeviceItem(
                            icon = if (peer.transportType == MeshNode.TRANSPORT_BLE) Icons.Outlined.Bluetooth else Icons.Outlined.Wifi,
                            name = peer.alias,
                            status = if (peer.hopDistance == 1) "Direct Connection" else "${peer.hopDistance} hops via Mesh",
                            strength = if (peer.rssi > -60) "Strong" else if (peer.rssi > -80) "Stable" else "Weak",
                            strengthColor = if (peer.rssi > -60) StatusActive else if (peer.rssi > -80) StatusStable else AlertPink
                        )
                        if (index < minOf(peers.size, 5) - 1) {
                            HorizontalDivider(color = DividerColor, thickness = 1.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceItem(icon: ImageVector, name: String, status: String, strength: String, strengthColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = StatusActive, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = name, color = TextPrimary, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = status, color = TextSecondary, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
            }
        }
        Text(text = strength, color = strengthColor, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun SOSFab(onNavigate: (Screen) -> Unit) {
    FloatingActionButton(
        onClick = { onNavigate(Screen.SEND_SOS) },
        containerColor = AlertPink,
        contentColor = Color.Black,
        shape = CircleShape,
        modifier = Modifier.size(64.dp)
    ) {
        Icon(imageVector = Icons.Outlined.Emergency, contentDescription = "SOS", modifier = Modifier.size(32.dp))
    }
}



@Composable
fun ZeroGridDashboardScreen() = MeshDashboardScreen()

@Preview(showBackground = true)
@Composable
fun ZeroGridDashboardPreview() {
    ZeroGridTheme {
        ZeroGridDashboardScreen()
    }
}