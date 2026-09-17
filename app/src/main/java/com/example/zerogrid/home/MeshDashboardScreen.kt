package com.example.zerogrid.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Emergency
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zerogrid.mesh.engine.MeshEngine
import com.example.zerogrid.mesh.engine.MeshNode
import com.example.zerogrid.navigation.Screen
import com.example.zerogrid.navigation.ZeroGridBottomBar
import com.example.zerogrid.ui.components.MeshConnectionState
import com.example.zerogrid.ui.components.PlainLanguageInfoCard
import com.example.zerogrid.ui.components.ZeroGridBadge
import com.example.zerogrid.ui.components.ZeroGridSkeletonItem
import com.example.zerogrid.ui.theme.SosRed

@Composable
fun MeshDashboardScreen(onNavigate: (Screen) -> Unit = {}) {
    val meshEngine = MeshEngine.getInstance(LocalContext.current)
    val peers by meshEngine.connectedPeers.collectAsState()
    val isMeshActive by meshEngine.isMeshActive.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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

            MeshStatusCard(peersCount = peers.size, isMeshActive = isMeshActive, onNavigate = onNavigate)

            Spacer(modifier = Modifier.height(14.dp))

            PlainLanguageInfoCard(
                title = "ZeroGrid Mesh Status",
                explanation = "You are connected to surrounding ZeroGrid nodes without internet. Emergency SOS signals automatically broadcast across all nearby devices."
            )

            Spacer(modifier = Modifier.height(16.dp))

            QuickActionsGrid(peersCount = peers.size, onNavigate = onNavigate)

            Spacer(modifier = Modifier.height(20.dp))

            NearbyDevicesSection(peers = peers, isMeshActive = isMeshActive, onNavigate = onNavigate)

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun DashboardTopBar(isMeshActive: Boolean) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ZeroGrid",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                ZeroGridBadge(
                    state = if (isMeshActive) MeshConnectionState.CONNECTED else MeshConnectionState.OFFLINE,
                    customText = if (isMeshActive) "Mesh Active" else "Mesh Offline"
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.SignalCellularAlt,
                    contentDescription = "Signal",
                    tint = if (isMeshActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
    }
}

@Composable
private fun MeshStatusCard(peersCount: Int, isMeshActive: Boolean, onNavigate: (Screen) -> Unit = {}) {
    Card(
        onClick = { onNavigate(Screen.NETWORK_STATUS) },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ZeroGridBadge(
                    state = if (!isMeshActive) MeshConnectionState.OFFLINE else if (peersCount > 0) MeshConnectionState.CONNECTED else MeshConnectionState.SEARCHING
                )
                Text(
                    text = "Topology >",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metrics Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricCard("Active Peers", peersCount.toString(), Modifier.weight(1f))
                MetricCard("Reachability", if (peersCount > 0) "Multi-Hop" else "Scanning", Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "✓ Off-grid mesh operation active",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun MetricCard(label: String, value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(text = label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun QuickActionsGrid(peersCount: Int, onNavigate: (Screen) -> Unit) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.ChatBubbleOutline,
                title = "Messages",
                subtitle = "Direct & Channels",
                iconTint = MaterialTheme.colorScheme.primary,
                onClick = { onNavigate(Screen.MESSAGES) }
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Share,
                title = "Mesh Devices",
                subtitle = "$peersCount connected",
                iconTint = MaterialTheme.colorScheme.primary,
                onClick = { onNavigate(Screen.MESH) }
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Folder,
                title = "Offline Files",
                subtitle = "P2P Transfers",
                iconTint = MaterialTheme.colorScheme.primary,
                onClick = { onNavigate(Screen.FILES) }
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Emergency,
                title = "SOS Beacon",
                subtitle = "Emergency Alert",
                iconTint = SosRed,
                borderColor = SosRed.copy(alpha = 0.4f),
                subtitleColor = SosRed,
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
    iconTint: Color,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(105.dp)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            if (subtitle != null) {
                Text(text = subtitle, color = subtitleColor, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun NearbyDevicesSection(peers: List<MeshNode>, isMeshActive: Boolean, onNavigate: (Screen) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Discovered Mesh Nodes", color = MaterialTheme.colorScheme.onBackground, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = { onNavigate(Screen.MESH) }) {
                Text(text = "Radar View", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                if (peers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Scanning for nearby peers...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            ZeroGridSkeletonItem(height = 12.dp, width = 160.dp)
                        }
                    }
                } else {
                    peers.take(5).forEachIndexed { index, peer ->
                        DeviceItem(
                            icon = if (peer.transportType == MeshNode.TRANSPORT_BLE) Icons.Outlined.Bluetooth else Icons.Outlined.Wifi,
                            name = peer.alias,
                            status = if (peer.hopDistance == 1) "Direct Connection" else "${peer.hopDistance} hops via Relay",
                            strength = if (peer.rssi > -60) "Strong Signal" else "Stable Signal",
                            strengthColor = MaterialTheme.colorScheme.primary
                        )
                        if (index < minOf(peers.size, 5) - 1) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
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
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = name, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = status, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
        Text(text = strength, color = strengthColor, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SOSFab(onNavigate: (Screen) -> Unit) {
    FloatingActionButton(
        onClick = { onNavigate(Screen.SEND_SOS) },
        containerColor = SosRed,
        contentColor = Color.White,
        shape = CircleShape,
        modifier = Modifier.size(60.dp)
    ) {
        Icon(imageVector = Icons.Outlined.Emergency, contentDescription = "SOS", modifier = Modifier.size(30.dp))
    }
}
