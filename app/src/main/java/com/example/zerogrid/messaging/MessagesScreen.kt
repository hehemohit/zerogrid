package com.example.zerogrid.messaging

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zerogrid.mesh.engine.MeshEngine
import com.example.zerogrid.mesh.engine.MeshNode
import com.example.zerogrid.navigation.Screen
import com.example.zerogrid.navigation.ZeroGridBottomBar
import com.example.zerogrid.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun MessagesScreen(
    onNavigate: (Screen) -> Unit = {},
    onOpenPeerChat: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val meshEngine = remember { MeshEngine.getInstance(context) }
    val connectedPeers by meshEngine.connectedPeers.collectAsState()
    val sosAlerts by meshEngine.sosAlerts.collectAsState()
    val conversations by meshEngine.conversations.collectAsState()
    val receivedMessages by meshEngine.receivedMessages.collectAsState()

    val channelMessages = receivedMessages.filter { it.recipientId == "*" }
    val peerCount = connectedPeers.size
    val activeAlertCount = sosAlerts.size

    // Peer IDs that have at least one persisted message (DM history)
    val dmPeerIds = remember(conversations) {
        conversations.entries
            .filter { it.value.isNotEmpty() }
            .sortedByDescending { it.value.last().timestamp }
            .map { it.key }
    }

    var selectedFilter by remember { mutableStateOf("All") }

    Scaffold(
        containerColor = DarkBackground,
        topBar = { MessagesTopBar() },
        bottomBar = { ZeroGridBottomBar(currentScreen = Screen.MESSAGES, onNavigate = onNavigate) },
        floatingActionButton = { NewMessageFab() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Dynamic mesh status bar
            MeshActiveStatusBar(peerCount = peerCount)
            Spacer(modifier = Modifier.height(16.dp))

            MessageFilterChipsRow(selected = selectedFilter, onSelected = { selectedFilter = it })
            Spacer(modifier = Modifier.height(20.dp))

            // ── ACTIVE DEVICES PANEL ──────────────────────────────────────────
            if (selectedFilter == "All" || selectedFilter == "Private") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ACTIVE DEVICES",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$peerCount online",
                        color = if (peerCount > 0) StatusActive else TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (connectedPeers.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.WifiOff,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "No devices in range — scanning...",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp)
                    ) {
                        items(connectedPeers, key = { it.nodeId }) { peer ->
                            ActiveDeviceCard(
                                peer = peer,
                                onClick = { onOpenPeerChat?.invoke(peer.nodeId) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── CHANNELS SECTION ──────────────────────────────────────────────
            if (selectedFilter == "All" || selectedFilter == "Channels") {
                Text(
                    text = "CHANNELS",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                ChannelsSection(
                    peerCount = peerCount,
                    alertCount = activeAlertCount,
                    channelMessageCount = channelMessages.size,
                    onMeshTap = { onNavigate(Screen.CHANNELS) },
                    onSosTap = { onNavigate(Screen.SOS_CENTER) }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // ── DIRECT MESSAGES SECTION ───────────────────────────────────────
            if (selectedFilter == "All" || selectedFilter == "Private") {
                Text(
                    text = "DIRECT MESSAGES",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                DirectMessagesSection(
                    dmPeerIds = dmPeerIds,
                    conversations = conversations,
                    connectedPeers = connectedPeers,
                    onOpenChat = { peerId -> onOpenPeerChat?.invoke(peerId) }
                )
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ActiveDeviceCard(peer: MeshNode, onClick: () -> Unit) {
    val initial = peer.alias.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    val isDirectBle = peer.transportType == MeshNode.TRANSPORT_BLE
    val transportIcon = if (isDirectBle) Icons.Outlined.Bluetooth else Icons.Outlined.Wifi
    val signalColor = when {
        peer.rssi >= -60 -> Color(0xFF4CAF50)
        peer.rssi >= -80 -> StatusActive
        else -> Color(0xFFFF9800)
    }

    Card(
        modifier = Modifier
            .width(100.dp)
            .border(1.dp, DividerColor, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SurfaceDarker, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    color = StatusActive,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                // Online green dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF4CAF50), CircleShape)
                        .border(1.5.dp, DarkBackground, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = peer.alias.take(10),
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(imageVector = transportIcon, contentDescription = null, tint = signalColor, modifier = Modifier.size(10.dp))
                Text(text = "${peer.rssi}dBm", color = signalColor, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(StatusActive.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                    .clickable { onClick() }
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CHAT",
                    color = StatusActive,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun ChannelsSection(
    peerCount: Int,
    alertCount: Int,
    channelMessageCount: Int,
    onMeshTap: () -> Unit,
    onSosTap: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // #mesh channel — dynamic peer count
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onMeshTap() },
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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(SurfaceDarker, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "#", color = StatusActive, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "mesh", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "General mesh communication", color = TextSecondary, fontSize = 13.sp)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "$peerCount peers", color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    if (channelMessageCount > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$channelMessageCount msgs",
                            color = StatusActive,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .background(SurfaceDarker, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // SOS channel — dynamic alert count
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSosTap() },
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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF3B1A1E), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Outlined.Campaign, contentDescription = null, tint = AlertPink, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "SOS", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            if (alertCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(modifier = Modifier.size(5.dp).background(AlertPink, CircleShape))
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Emergency broadcasts", color = TextSecondary, fontSize = 13.sp)
                    }
                }
                if (alertCount > 0) {
                    Text(
                        text = "$alertCount alert${if (alertCount != 1) "s" else ""}",
                        color = AlertPink,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .background(Color(0xFF3B1A1E), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                } else {
                    Text(text = "Clear", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun DirectMessagesSection(
    dmPeerIds: List<String>,
    conversations: Map<String, List<StoredMessage>>,
    connectedPeers: List<MeshNode>,
    onOpenChat: (String) -> Unit
) {
    if (dmPeerIds.isEmpty()) {
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
                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = "No direct messages yet", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tap a device above to start a conversation",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            dmPeerIds.forEach { peerId ->
                val msgs = conversations[peerId] ?: emptyList()
                val lastMsg = msgs.lastOrNull() ?: return@forEach
                val peer = connectedPeers.firstOrNull { it.nodeId == peerId }
                val name = peer?.alias ?: "Peer ${peerId.takeLast(6)}"
                val isOnline = peer != null
                val unread = msgs.count { !it.isMine }
                val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(lastMsg.timestamp)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenChat(peerId) },
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(SurfaceDarker, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name.first().uppercaseChar().toString(),
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .background(Color(0xFF4CAF50), CircleShape)
                                        .border(1.5.dp, DarkBackground, CircleShape)
                                        .align(Alignment.BottomEnd)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text(text = timeStr, color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${if (lastMsg.isMine) "You: " else ""}${lastMsg.text}",
                                color = TextSecondary,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = if (isOnline) "Online" else "Last seen",
                                    color = if (isOnline) Color(0xFF4CAF50) else TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .background(SurfaceDarker, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                                Text(
                                    text = "${msgs.size} msgs",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .background(SurfaceDarker, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (unread > 0) {
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .background(StatusActive, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (unread > 9) "9+" else unread.toString(),
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MessagesTopBar() {
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
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "Mesh",
                    tint = StatusActive,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "ZeroGrid",
                    color = StatusActive,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = StatusActive,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit",
                    tint = StatusActive,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
    }
}

@Composable
private fun MeshActiveStatusBar(peerCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).background(StatusActive, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MESH ACTIVE  •  $peerCount PEER${if (peerCount != 1) "S" else ""}",
                    color = StatusActive,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                modifier = Modifier
                    .background(SurfaceDarker, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = StatusActive,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Encrypted",
                    color = StatusActive,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MessageFilterChipsRow(selected: String, onSelected: (String) -> Unit) {
    val filters = listOf("All", "Private", "Channels")
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
private fun NewMessageFab() {
    FloatingActionButton(
        onClick = { },
        containerColor = StatusActive,
        contentColor = Color.Black,
        shape = CircleShape,
        modifier = Modifier.size(64.dp)
    ) {
        Icon(imageVector = Icons.AutoMirrored.Outlined.Chat, contentDescription = "New Message", modifier = Modifier.size(28.dp))
    }
}

@Composable
fun ZeroGridMessagesScreen() = MessagesScreen()