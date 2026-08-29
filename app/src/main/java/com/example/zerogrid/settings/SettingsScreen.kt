package com.example.zerogrid

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import com.example.zerogrid.navigation.Screen
import com.example.zerogrid.navigation.ZeroGridBottomBar
import com.example.zerogrid.ui.theme.*

@Composable
fun SettingsScreen(onNavigate: (Screen) -> Unit = {}) {
    var meshDiscoveryEnabled by remember { mutableStateOf(true) }
    var automaticSwitchingEnabled by remember { mutableStateOf(true) }
    var relayModeEnabled by remember { mutableStateOf(true) }
    var emergencyAlertsEnabled by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = { SettingsTopBar() },
        bottomBar = { ZeroGridBottomBar(currentScreen = Screen.SETTINGS, onNavigate = onNavigate) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // User Profile Card
            UserProfileCard()
            Spacer(modifier = Modifier.height(24.dp))

            // Network Section
            Text(
                text = "NETWORK",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DividerColor)
            ) {
                Column {
                    SettingsNavigationRow(
                        title = "Network Mode",
                        subtitle = "Automatic",
                        onClick = { }
                    )
                    HorizontalDivider(color = DividerColor, thickness = 1.dp)
                    SettingsSwitchRow(
                        title = "Mesh Discovery",
                        subtitle = "Active",
                        checked = meshDiscoveryEnabled,
                        onCheckedChange = { meshDiscoveryEnabled = it }
                    )
                    HorizontalDivider(color = DividerColor, thickness = 1.dp)
                    SettingsSwitchRow(
                        title = "Automatic Switching",
                        subtitle = "LAN ↔ Wi-Fi Direct",
                        checked = automaticSwitchingEnabled,
                        onCheckedChange = { automaticSwitchingEnabled = it }
                    )
                    HorizontalDivider(color = DividerColor, thickness = 1.dp)
                    SettingsSwitchRow(
                        title = "Relay Mode",
                        subtitle = "Forward encrypted traffic",
                        checked = relayModeEnabled,
                        onCheckedChange = { relayModeEnabled = it },
                        isLast = true
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // Communication Section
            Text(
                text = "COMMUNICATION",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DividerColor)
            ) {
                Column {
                    SettingsNavigationRow(
                        title = "Notifications",
                        subtitle = "Enabled",
                        onClick = { }
                    )
                    HorizontalDivider(color = DividerColor, thickness = 1.dp)
                    SettingsSwitchRow(
                        title = "Emergency Alerts",
                        subtitle = "Enabled",
                        checked = emergencyAlertsEnabled,
                        onCheckedChange = { emergencyAlertsEnabled = it },
                        isLast = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) // Padding for bottom nav
        }
    }
}

@Composable
private fun SettingsTopBar() {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Settings",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Icon(
                imageVector = Icons.Outlined.Security,
                contentDescription = "Security",
                tint = TextPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
    }
}

@Composable
private fun UserProfileCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(SurfaceDarker, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Devices,
                        contentDescription = null,
                        tint = StatusActive,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Alex",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "ZeroGrid Device",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(StatusActive, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Device ID: ZG-7A42-••••",
                            color = StatusActive,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Navigate",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsNavigationRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = "Navigate",
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 13.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = StatusActive,
                uncheckedThumbColor = TextSecondary,
                uncheckedTrackColor = SurfaceDarker,
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}

@Composable
private fun DashboardBottomNavSettingsActive() {
    NavigationBar(
        containerColor = BottomNavBg,
        contentColor = TextSecondary,
        tonalElevation = 0.dp
    ) {
        val items = listOf(
            Triple("Home", Icons.Outlined.Home, false),
            Triple("Messages", Icons.Outlined.ChatBubbleOutline, false),
            Triple("Mesh", Icons.Outlined.Share, false),
            Triple("Files", Icons.Outlined.Folder, false),
            Triple("Settings", Icons.Outlined.Settings, true)
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

@Composable
fun ZeroGridSettingsScreen() = SettingsScreen()

@Preview(showBackground = true)
@Composable
fun ZeroGridSettingsPreview() {
    ZeroGridTheme {
        ZeroGridSettingsScreen()
    }
}