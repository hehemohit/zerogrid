package com.example.zerogrid

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zerogrid.ui.theme.*

@Composable
fun MeshDashboardScreen() {
    Scaffold(
        containerColor = DarkBackground,
        topBar = { DashboardTopBar() },
        bottomBar = { DashboardBottomNav() },
        floatingActionButton = { SOSFab() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            MeshStatusCard()
            Spacer(modifier = Modifier.height(16.dp))
            QuickActionsGrid()
            Spacer(modifier = Modifier.height(24.dp))
            NearbyDevicesSection()
            Spacer(modifier = Modifier.height(32.dp)) // Extra space for FAB
        }
    }
}

@Composable
private fun DashboardTopBar() {
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
                    tint = StatusActive,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ZeroGrid",
                    color = StatusActive,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Mesh Active Pill
                Row(
                    modifier = Modifier
                        .background(Color(0xFF1A3B40), RoundedCornerShape(16.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(StatusActive, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Mesh Active",
                        color = StatusActive,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.SignalCellularAlt,
                    contentDescription = "Signal",
                    tint = StatusActive,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
    }
}

@Composable
private fun MeshStatusCard() {
    Card(
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
                        text = "MESH CONNECTED",
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
                text = "Wi-Fi Direct Active",
                color = TextSecondary,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            // Metrics Grid
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Peers", "12", Modifier.weight(1f))
                MetricCard("Routes", "3", Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricCard("Relays", "2", Modifier.weight(1f))
                MetricCard("Latency", "45ms", Modifier.weight(1f))
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
private fun QuickActionsGrid() {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.ChatBubbleOutline,
                title = "Messages",
                badgeText = "3 unread",
                iconTint = StatusActive
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Share,
                title = "Mesh Network",
                subtitle = "12 peers",
                iconTint = StatusActive
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Folder,
                title = "Files",
                subtitle = "2 active transfers",
                iconTint = StatusActive
            )
            QuickActionCard(
                modifier = Modifier.weight(1f),
                icon = Icons.Outlined.Emergency, // Using Emergency for asterisk
                title = "SOS",
                subtitle = "2 active alerts",
                iconTint = AlertPink,
                borderColor = AlertRedBorder,
                subtitleColor = AlertPink
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
    subtitleColor: Color = TextSecondary
) {
    Card(
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
private fun NearbyDevicesSection() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Nearby Devices", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = "View All", color = StatusActive, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
        }
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column {
                DeviceItem(Icons.Outlined.Person, "Alex", "Direct Connection", "Strong", StatusActive)
                HorizontalDivider(color = DividerColor, thickness = 1.dp)
                DeviceItem(Icons.Outlined.Group, "Rescue Team", "2 hops • via Device-7A42", "Stable", StatusStable)
                HorizontalDivider(color = DividerColor, thickness = 1.dp)
                DeviceItem(Icons.Outlined.Router, "Device-7A42", "Relay • Direct", "Strong", StatusActive)
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
private fun SOSFab() {
    FloatingActionButton(
        onClick = { /* Trigger SOS */ },
        containerColor = AlertPink,
        contentColor = Color.Black,
        shape = CircleShape,
        modifier = Modifier.size(64.dp)
    ) {
        Icon(imageVector = Icons.Outlined.Emergency, contentDescription = "SOS", modifier = Modifier.size(32.dp))
    }
}

@Composable
private fun DashboardBottomNav() {
    NavigationBar(
        containerColor = BottomNavBg,
        contentColor = TextSecondary,
        tonalElevation = 0.dp
    ) {
        val items = listOf(
            Triple("Home", Icons.Outlined.Home, true),
            Triple("Messages", Icons.Outlined.ChatBubbleOutline, false),
            Triple("Mesh", Icons.Outlined.Share, false),
            Triple("Files", Icons.Outlined.Folder, false),
            Triple("Settings", Icons.Outlined.Settings, false)
        )
        items.forEach { (label, icon, selected) ->
            NavigationBarItem(
                selected = selected,
                onClick = { },
                icon = { Icon(imageVector = icon, contentDescription = label) },
                label = { Text(text = label, fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    unselectedIconColor = TextSecondary,
                    selectedTextColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = StatusActive
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ZeroGridDashboardPreview() {
    ZeroGridTheme {
        ZeroGridDashboardScreen()
    }
}