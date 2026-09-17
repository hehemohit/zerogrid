package com.example.zerogrid.mesh

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.outlined.Bluetooth
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import com.example.zerogrid.ui.components.ZeroGridListSkeleton

@Composable
fun NearbyDevicesScreen(onNavigate: (Screen) -> Unit = {}) {
    var selectedFilter by remember { mutableStateOf("All") }
    val meshEngine = MeshEngine.getInstance(LocalContext.current)
    val peers by meshEngine.connectedPeers.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { NearbyTopBar(onBack = { onNavigate(Screen.HOME) }) },
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

            MeshDiscoveryCard(peers.size)

            Spacer(modifier = Modifier.height(12.dp))

            PlainLanguageInfoCard(
                title = "Understanding Mesh Distance",
                explanation = "Devices listed as 'Direct' are within radio range. Devices listed with '2+ Hops' are reached by automatically bouncing through intermediate phones."
            )

            Spacer(modifier = Modifier.height(16.dp))
            RadarGraphicCard()

            Spacer(modifier = Modifier.height(16.dp))
            FilterChipsRow(selected = selectedFilter, onSelected = { selectedFilter = it })

            Spacer(modifier = Modifier.height(16.dp))
            DevicesListSection(peers = peers, onNavigate = onNavigate)

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NearbyTopBar(onBack: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Nearby Mesh Devices",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
    }
}

@Composable
private fun MeshDiscoveryCard(devicesFound: Int) {
    Card(
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
                ZeroGridBadge(state = MeshConnectionState.SEARCHING, customText = "Scanning Radios")
                Text(
                    text = "$devicesFound Found",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Radios Active: BLE Scanning • Wi-Fi Direct Peer Search",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun RadarGraphicCard() {
    val outlineColor = MaterialTheme.colorScheme.outline
    val surfaceDarker = MaterialTheme.colorScheme.surfaceVariant
    val primaryColor = MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                drawCircle(color = surfaceDarker, radius = 45.dp.toPx(), center = center)
                drawCircle(color = outlineColor, radius = 70.dp.toPx(), center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f))
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(surfaceDarker, CircleShape)
                    .border(1.dp, primaryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = "Device",
                    tint = primaryColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun FilterChipsRow(selected: String, onSelected: (String) -> Unit) {
    val filters = listOf("All", "Direct", "Multi-Hop")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = filter == selected
            Button(
                onClick = { onSelected(filter) },
                modifier = Modifier
                    .height(38.dp)
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(19.dp),
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
private fun DevicesListSection(peers: List<MeshNode>, onNavigate: (Screen) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (peers.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Column {
                    Text(
                        text = "Searching for nearby ZeroGrid devices...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    ZeroGridListSkeleton(itemCount = 2)
                }
            }
        } else {
            peers.forEach { peer ->
                DeviceCard(
                    icon = if (peer.transportType == MeshNode.TRANSPORT_BLE) Icons.Outlined.Bluetooth else Icons.Outlined.Wifi,
                    name = peer.alias,
                    status = if (peer.hopDistance == 1) "Direct • ${peer.transportType}" else "${peer.hopDistance} Relay Hops",
                    subStatus = "Signal: ${peer.rssi} dBm",
                    onViewClick = { onNavigate(Screen.PEER_DETAILS) }
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
    subStatus: String,
    onViewClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
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
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = name, color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = status, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = subStatus, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = onViewClick,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(text = "Peer Info", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}
