package com.example.zerogrid.messaging

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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.zerogrid.mesh.engine.MeshEngine
import com.example.zerogrid.navigation.Screen
import com.example.zerogrid.navigation.ZeroGridBottomBar
import com.example.zerogrid.ui.theme.*

@Composable
fun ChannelsScreen(onNavigate: (Screen) -> Unit = {}) {
    var selectedFilter by remember { mutableStateOf("All") }
    val meshEngine = MeshEngine.getInstance(LocalContext.current)
    val peers by meshEngine.connectedPeers.collectAsState()
    val alerts by meshEngine.sosAlerts.collectAsState()

    Scaffold(
        containerColor = DarkBackground,
        topBar = { ChannelsTopBar(onBackClick = { onNavigate(Screen.MESSAGES) }) },
        bottomBar = { ZeroGridBottomBar(currentScreen = Screen.MESSAGES, onNavigate = onNavigate) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            MeshActiveStatusBarChannels(peersCount = peers.size)
            Spacer(modifier = Modifier.height(16.dp))
            ChannelFilterChipsRow(selected = selectedFilter, onSelected = { selectedFilter = it })
            Spacer(modifier = Modifier.height(20.dp))

            if (selectedFilter == "All" || selectedFilter == "Emergency") {
                Text(
                    text = "EMERGENCY",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                EmergencyChannelSection(
                    alertsCount = alerts.size,
                    onClick = { onNavigate(Screen.SOS_CENTER) }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            if (selectedFilter == "All" || selectedFilter == "Public") {
                Text(
                    text = "PUBLIC",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                PublicChannelsSection(
                    peersCount = peers.size,
                    onOpenBroadcast = { onNavigate(Screen.MESSAGES) }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            if (selectedFilter == "All" || selectedFilter == "Private") {
                Text(
                    text = "PRIVATE",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                PrivateChannelsSection(
                    onOpenDirect = { onNavigate(Screen.MESH) }
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Channels are local to the ZeroGrid mesh.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ChannelsTopBar(onBackClick: () -> Unit = {}) {
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
                    text = "Channels",
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
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Add",
                    tint = StatusActive,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
    }
}

@Composable
private fun MeshActiveStatusBarChannels(peersCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(modifier = Modifier.size(6.dp).background(StatusActive, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Mesh Active",
            color = StatusActive,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "  |  $peersCount device${if (peersCount != 1) "s" else ""} reachable",
            color = TextSecondary,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun ChannelFilterChipsRow(selected: String, onSelected: (String) -> Unit) {
    val filters = listOf("All", "Public", "Private", "Emergency")
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
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun EmergencyChannelSection(alertsCount: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, if (alertsCount > 0) AlertRedBorder else DividerColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#SOS",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (alertsCount > 0) "Priority alert" else "Priority standby",
                        color = if (alertsCount > 0) AlertPink else StatusActive,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .background(if (alertsCount > 0) Color(0xFF3B1A1E) else SurfaceDarker, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.Emergency,
                    contentDescription = null,
                    tint = if (alertsCount > 0) AlertPink else StatusActive,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Emergency mesh broadcasts", color = TextSecondary, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).background(if (alertsCount > 0) AlertPink else StatusActive, CircleShape))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (alertsCount > 0) "$alertsCount active alert${if (alertsCount != 1) "s" else ""}" else "No active alerts • Standby",
                    color = if (alertsCount > 0) AlertPink else StatusActive,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun PublicChannelsSection(peersCount: Int, onOpenBroadcast: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PublicChannelCard(
            name = "#mesh",
            desc = "General decentralized mesh broadcast",
            participants = "${peersCount + 1} in reach",
            icon = Icons.Outlined.Share,
            onClick = onOpenBroadcast
        )
        PublicChannelCard(
            name = "#community",
            desc = "Local community announcements",
            participants = "${peersCount + 1} in reach",
            icon = null,
            onClick = onOpenBroadcast
        )
        PublicChannelCard(
            name = "#rescue",
            desc = "Rescue and emergency response coordination",
            participants = "${peersCount + 1} in reach",
            lastActivity = "Active on Mesh",
            icon = null,
            onClick = onOpenBroadcast
        )
    }
}

@Composable
private fun PublicChannelCard(
    name: String,
    desc: String,
    participants: String,
    lastActivity: String? = null,
    icon: ImageVector?,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    if (icon != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(imageVector = icon, contentDescription = null, tint = StatusActive, modifier = Modifier.size(16.dp))
                    }
                }
                Text(
                    text = "Public",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .background(SurfaceDarker, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = desc, color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Outlined.Group, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = participants, color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
                if (lastActivity != null) {
                    Text(text = lastActivity, color = StatusActive, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun PrivateChannelsSection(onOpenDirect: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        PrivateChannelCard(
            name = "#medical-team",
            desc = "Encrypted peer team channel",
            participants = "End-to-End Encrypted",
            onClick = onOpenDirect
        )
        PrivateChannelCard(
            name = "#volunteers",
            desc = "Encrypted peer coordination channel",
            participants = "End-to-End Encrypted",
            onClick = onOpenDirect
        )
    }
}

@Composable
private fun PrivateChannelCard(
    name: String,
    desc: String,
    participants: String,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = name,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(imageVector = Icons.Outlined.Lock, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                }
                Text(
                    text = "Encrypted",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .background(SurfaceDarker, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = desc, color = TextSecondary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Outlined.Shield, contentDescription = null, tint = StatusActive, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = participants, color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Outlined.Key, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Direct Mesh", color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}



@Composable
fun ZeroGridChannelsScreen() = ChannelsScreen()

@Preview(showBackground = true)
@Composable
fun ZeroGridChannelsPreview() {
    ZeroGridTheme {
        ZeroGridChannelsScreen()
    }
}