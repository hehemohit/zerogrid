package com.example.zerogrid.mesh

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun PeerDetailsScreen(onNavigate: (Screen) -> Unit = {}) {
    val context = LocalContext.current
    val meshEngine = remember { MeshEngine.getInstance(context) }
    val connectedPeers by meshEngine.connectedPeers.collectAsState()
    val peer = connectedPeers.firstOrNull()

    var trustDevice by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = { PeerDetailsTopBar(onBackClick = { onNavigate(Screen.MESH) }) },
        bottomBar = { ZeroGridBottomBar(currentScreen = Screen.MESH, onNavigate = onNavigate) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            PeerHeaderSection(peer = peer)
            Spacer(modifier = Modifier.height(24.dp))
            ActionButtonsRow(
                onMessageClick = { onNavigate(Screen.CHAT_DETAIL) },
                onSendFileClick = { onNavigate(Screen.SEND_FILE) }
            )
            Spacer(modifier = Modifier.height(24.dp))
            ConnectionInfoCard(peer = peer)
            Spacer(modifier = Modifier.height(20.dp))
            IdentitySecurityCard(peer = peer)
            Spacer(modifier = Modifier.height(16.dp))
            TrustDeviceCard(checked = trustDevice, onCheckedChange = { trustDevice = it })
            Spacer(modifier = Modifier.height(16.dp))
            MenuNavigationItem(
                icon = Icons.Outlined.Folder,
                title = "View Shared Files",
                titleColor = TextPrimary,
                onClick = { onNavigate(Screen.FILES) }
            )
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PeerDetailsTopBar(onBackClick: () -> Unit = {}) {
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
                    text = "Peer Details",
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
private fun PeerHeaderSection(peer: MeshNode?) {
    val name = peer?.alias?.ifEmpty { peer.nodeId } ?: "Discovered Mesh Node"
    val hopsText = if (peer != null) {
        if (peer.isDirectNeighbor) "Connected • Direct neighbor" else "Connected • ${peer.hopDistance} hops away"
    } else {
        "Searching for mesh peers..."
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(SurfaceDarker, CircleShape)
                .border(1.dp, StatusActive, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (peer?.isDirectNeighbor == true) Icons.Outlined.Person else Icons.Outlined.Router,
                contentDescription = "Device",
                tint = StatusActive,
                modifier = Modifier.size(36.dp)
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(if (peer != null) StatusActive else TextSecondary, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = name,
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).background(if (peer != null) StatusActive else TextSecondary, CircleShape))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = hopsText,
                color = StatusActive,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "ZeroGrid Device",
            color = TextSecondary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .background(Color(0xFF1A3B40), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = "Secure",
                tint = StatusActive,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "End-to-End Encrypted",
                color = StatusActive,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ActionButtonsRow(
    onMessageClick: () -> Unit,
    onSendFileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onMessageClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StatusActive, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Message", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = onSendFileClick,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusActive),
            border = androidx.compose.foundation.BorderStroke(1.dp, StatusActive),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Outlined.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Send File", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ConnectionInfoCard(peer: MeshNode?) {
    val transport = peer?.transportType ?: "BLE & Wi-Fi Direct"
    val hops = if (peer != null) "${peer.hopDistance} hops" else "1 hop"
    val signalBars = if (peer == null) 2 else if (peer.rssi > -60) 4 else if (peer.rssi > -80) 3 else 2

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "CONNECTION",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            ConnectionRow("Transport", transport)
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            ConnectionRow("Status", if (peer != null) "Connected" else "Scanning")
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            ConnectionSignalRow("Signal", if (signalBars >= 3) "Good" else "Fair", signalBars)
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            ConnectionRow("Hop Distance", hops)
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            ConnectionRow("Last Seen", "Just now", valueColor = StatusActive)
        }
    }
}

@Composable
private fun ConnectionRow(label: String, value: String, valueColor: Color = TextPrimary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 14.sp)
        Text(text = value, color = valueColor, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ConnectionSignalRow(label: String, value: String, signalBars: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                for (i in 1..4) {
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(14.dp)
                            .background(
                                if (i <= signalBars) StatusActive else SurfaceDarker,
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
            Text(text = value, color = TextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun IdentitySecurityCard(peer: MeshNode?) {
    val nodeId = peer?.nodeId ?: "NODE-LOCAL"
    val fingerprint = "${nodeId.take(4).uppercase()} •••• ${nodeId.takeLast(4).uppercase()}"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "IDENTITY & SECURITY",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Outlined.VerifiedUser, contentDescription = null, tint = StatusActive, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Peer Identity Encrypted", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Verified via Mesh", color = StatusActive, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            ConnectionRow("Device ID", nodeId)
            ConnectionRow("Fingerprint", fingerprint)
        }
    }
}

@Composable
private fun TrustDeviceCard(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Trust this device", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Allow ZeroGrid to recognize this peer as a trusted device.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = StatusActive,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = SurfaceDarker
                )
            )
        }
    }
}

@Composable
private fun MenuNavigationItem(icon: ImageVector, title: String, titleColor: Color, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = titleColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = title, color = titleColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
            Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun ZeroGridPeerDetailsScreen() = PeerDetailsScreen()

@Preview(showBackground = true)
@Composable
fun ZeroGridPeerDetailsPreview() {
    ZeroGridTheme {
        ZeroGridPeerDetailsScreen()
    }
}