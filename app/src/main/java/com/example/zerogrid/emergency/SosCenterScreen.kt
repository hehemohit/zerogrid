package com.example.zerogrid.emergency

import androidx.compose.foundation.BorderStroke
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
import com.example.zerogrid.mesh.engine.MeshPacket
import com.example.zerogrid.navigation.Screen
import com.example.zerogrid.navigation.ZeroGridBottomBar
import com.example.zerogrid.ui.theme.*

@Composable
fun SosCenterScreen(onNavigate: (Screen) -> Unit = {}) {
    val meshEngine = MeshEngine.getInstance(LocalContext.current)
    val alerts by meshEngine.sosAlerts.collectAsState()
    val peers by meshEngine.connectedPeers.collectAsState()

    Scaffold(
        containerColor = DarkBackground,
        topBar = { EmergencyTopBar(onBackClick = { onNavigate(Screen.HOME) }) },
        bottomBar = { ZeroGridBottomBar(currentScreen = Screen.SOS_CENTER, onNavigate = onNavigate) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            MeshStatusBanner(peers.size)
            Spacer(modifier = Modifier.height(16.dp))

            // Emergency SOS Action Card
            EmergencySosCard(onSendSosClick = { onNavigate(Screen.SEND_SOS) })
            Spacer(modifier = Modifier.height(24.dp))

            // Active Emergency Alerts Section
            Text(
                text = "ACTIVE EMERGENCY ALERTS",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            ActiveAlertsSection(alerts = alerts)

            Spacer(modifier = Modifier.height(24.dp))

            // Network Reach Section
            Text(
                text = "NETWORK REACH",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            NetworkReachSection(peers.size)

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Activity Section
            Text(
                text = "RECENT ACTIVITY",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            RecentActivitySection()

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Action Buttons Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Campaign,
                    title = "Broadcast\nSOS"
                )
                QuickActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.RssFeed,
                    title = "View SOS\nFeed"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Emergency Contacts Button
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DividerColor)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Outlined.ContactEmergency, contentDescription = null, tint = StatusActive, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Emergency Contacts",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // How SOS Works Footer Card
            HowSosWorksCard()

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun EmergencyTopBar(onBackClick: () -> Unit = {}) {
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
                    text = "Emergency Center",
                    color = StatusActive,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                imageVector = Icons.Outlined.History,
                contentDescription = "History",
                tint = StatusActive,
                modifier = Modifier.size(24.dp)
            )
        }
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
    }
}

@Composable
private fun MeshStatusBanner(reachableCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Box(modifier = Modifier.size(6.dp).background(StatusActive, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "ZeroGrid Mesh Active",
            color = StatusActive,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(16.dp))
        Icon(imageVector = Icons.Outlined.Hub, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "$reachableCount devices reachable",
            color = TextSecondary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun EmergencySosCard(onSendSosClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AlertRedBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFF3B1A1E), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Outlined.Warning, contentDescription = null, tint = AlertPink, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Emergency SOS",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Broadcast an emergency alert to nearby ZeroGrid devices.",
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onSendSosClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AlertPink),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Outlined.Campaign, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "SEND SOS",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Your SOS will be relayed across the local mesh. Use only for genuine emergencies.",
                color = TextSecondary,
                fontSize = 11.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun ActiveAlertsSection(alerts: List<MeshPacket>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (alerts.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(text = "No active emergency alerts.", color = TextSecondary, fontSize = 14.sp)
            }
        } else {
            alerts.forEach { alert ->
                Card(
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
                                Box(modifier = Modifier.size(6.dp).background(AlertPink, CircleShape))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Emergency Alert",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "ACTIVE",
                                color = AlertPink,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(Color(0xFF3B1A1E), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "From: ${alert.senderId.takeLast(8)} • ${alert.hopCount} hops",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = alert.payload,
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val timeText = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(alert.timestamp)
                            Text(
                                text = timeText,
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Button(
                                onClick = { },
                                modifier = Modifier
                                    .height(32.dp)
                                    .wrapContentWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarker),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "ACKNOWLEDGE",
                                    color = StatusActive,
                                    fontSize = 11.sp,
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
private fun NetworkReachSection(reachableCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Devices reached", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.Devices, contentDescription = null, tint = StatusActive, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = reachableCount.toString(),
                            color = StatusActive,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    // ... (rest of the card content)
                    Text(text = "Relay hops", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "3",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Delivery status", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "94%",
                        color = StatusActive,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Last broadcast", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "12:41 PM",
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentActivitySection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            RecentActivityItem(
                icon = Icons.Outlined.CheckCircle,
                iconTint = StatusActive,
                title = "SOS broadcast delivered (Reached 9 devices)",
                time = "12:28 PM"
            )
            RecentActivityItem(
                icon = Icons.Outlined.Shield,
                iconTint = StatusActive,
                title = "SOS acknowledged by Rescue Team",
                time = "12:24 PM"
            )
            RecentActivityItem(
                icon = Icons.Outlined.DoNotDisturbAlt,
                iconTint = TextSecondary,
                title = "Emergency alert expired",
                time = "11:52 AM"
            )
        }
    }
}

@Composable
private fun RecentActivityItem(icon: ImageVector, iconTint: Color, title: String, time: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(SurfaceDarker, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = TextPrimary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = time, color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun QuickActionButton(modifier: Modifier = Modifier, icon: ImageVector, title: String) {
    Card(
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = StatusActive, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                lineHeight = 16.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun HowSosWorksCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(imageVector = Icons.Outlined.Info, contentDescription = null, tint = StatusActive, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "How SOS works",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Your emergency message is encrypted and propagated through reachable ZeroGrid devices. Relayed hops ensure maximum reachability.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}



@Composable
fun ZeroGridEmergencyCenterScreen() = SosCenterScreen()

@Preview(showBackground = true)
@Composable
fun ZeroGridEmergencyCenterPreview() {
    ZeroGridTheme {
        ZeroGridEmergencyCenterScreen()
    }
}