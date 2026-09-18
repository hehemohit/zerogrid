package com.example.zerogrid.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
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
import com.example.zerogrid.navigation.*
import com.example.zerogrid.ui.theme.*
import com.zerogrid.mesh.app.ui.UserRole
import com.zerogrid.mesh.app.ui.UserSessionManager

private val DangerRed = Color(0xFFFF3B30)
private val AdminAmber = Color(0xFFFF9500)

@Composable
fun SettingsScreen(
    onNavigate: (Screen) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { UserSessionManager.getInstance(context) }

    var meshDiscoveryEnabled by remember { mutableStateOf(true) }
    var automaticSwitchingEnabled by remember { mutableStateOf(true) }
    var relayModeEnabled by remember { mutableStateOf(true) }
    var emergencyAlertsEnabled by remember { mutableStateOf(true) }

    // Edit name dialog state
    var showEditNameDialog by remember { mutableStateOf(false) }
    var editNameValue by remember { mutableStateOf(sessionManager.getUserName()) }
    var displayName by remember { mutableStateOf(sessionManager.getUserName()) }

    // Logout confirm dialog
    var showLogoutDialog by remember { mutableStateOf(false) }

    // ── Edit Name Dialog ─────────────────────────────────────────────────
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            containerColor = CardBackground,
            title = {
                Text("Edit Display Name", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = editNameValue,
                        onValueChange = { editNameValue = it },
                        label = { Text("Display Name", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = SurfaceDarker,
                            unfocusedContainerColor = SurfaceDarker,
                            focusedBorderColor = PrimaryCyan,
                            unfocusedBorderColor = DividerColor,
                            focusedLabelColor = PrimaryCyan
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Display name updated locally. Server sync coming soon.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = editNameValue.trim()
                    if (trimmed.isNotEmpty()) {
                        sessionManager.setUserName(trimmed)
                        displayName = trimmed
                    }
                    showEditNameDialog = false
                }) {
                    Text("Save", color = PrimaryCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // ── Logout Confirm Dialog ────────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            containerColor = CardBackground,
            title = {
                Text("Sign Out", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "Are you sure you want to sign out? You will need to log in again to access ZeroGrid.",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    sessionManager.clearSession()
                    onLogout()
                }) {
                    Text("Sign Out", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

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

            // ── Profile Card ─────────────────────────────────────────────
            ProfileCard(
                sessionManager = sessionManager,
                displayName = displayName,
                onEditClick = {
                    editNameValue = displayName
                    showEditNameDialog = true
                }
            )
            Spacer(modifier = Modifier.height(24.dp))

            // ── Account Section ───────────────────────────────────────────
            Text(
                text = "ACCOUNT",
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
                    // Edit display name row
                    Surface(
                        onClick = { editNameValue = displayName; showEditNameDialog = true },
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
                                Text("Edit Display Name", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                                Text(displayName.ifEmpty { "—" }, color = TextSecondary, fontSize = 13.sp)
                            }
                            Icon(Icons.Outlined.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                        }
                    }
                    HorizontalDivider(color = DividerColor)
                    // Account type
                    AccountInfoRow("Account Type", "STANDARD")
                    HorizontalDivider(color = DividerColor)
                    // Member since
                    AccountInfoRow("Member Since", "Sep 2026")
                    HorizontalDivider(color = DividerColor)
                    // Server User ID
                    AccountInfoRow(
                        "Server User ID",
                        "••••${sessionManager.getUserId()?.takeLast(8) ?: "--------"}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Network Section ───────────────────────────────────────────
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
                    SettingsNavigationRow(title = "Network Mode", subtitle = "Automatic", onClick = { })
                    HorizontalDivider(color = DividerColor)
                    SettingsSwitchRow(
                        title = "Mesh Discovery",
                        subtitle = "Active",
                        checked = meshDiscoveryEnabled,
                        onCheckedChange = { meshDiscoveryEnabled = it }
                    )
                    HorizontalDivider(color = DividerColor)
                    SettingsSwitchRow(
                        title = "Automatic Switching",
                        subtitle = "LAN ↔ Wi-Fi Direct",
                        checked = automaticSwitchingEnabled,
                        onCheckedChange = { automaticSwitchingEnabled = it }
                    )
                    HorizontalDivider(color = DividerColor)
                    SettingsSwitchRow(
                        title = "Relay Mode",
                        subtitle = "Forward encrypted traffic",
                        checked = relayModeEnabled,
                        onCheckedChange = { relayModeEnabled = it }
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            // ── Communication Section ─────────────────────────────────────
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
                    SettingsNavigationRow(title = "Notifications", subtitle = "Enabled", onClick = { })
                    HorizontalDivider(color = DividerColor)
                    SettingsNavigationRow(
                        title = "Security & Privacy",
                        subtitle = "Keys, E2EE, Anonymity",
                        onClick = { onNavigate(Screen.SECURITY_PRIVACY) }
                    )
                    HorizontalDivider(color = DividerColor)
                    SettingsSwitchRow(
                        title = "Emergency Alerts",
                        subtitle = "Enabled",
                        checked = emergencyAlertsEnabled,
                        onCheckedChange = { emergencyAlertsEnabled = it }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Developer Section ─────────────────────────────────────────
            Text(
                text = "DEVELOPER",
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
                border = BorderStroke(1.dp, Color(0xFF2A2D36))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(Screen.DEBUG_CONSOLE) }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF1A1A2E), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.BugReport, null, tint = Color(0xFF82B1FF), modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Debug Console", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Live BLE trace, MTU, packet log", color = TextSecondary, fontSize = 12.sp)
                    }
                    Icon(Icons.Outlined.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Danger Zone ───────────────────────────────────────────────
            Text(
                text = "DANGER ZONE",
                color = DangerRed.copy(alpha = 0.7f),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLogoutDialog = true }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(DangerRed.copy(alpha = 0.1f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Outlined.ExitToApp, null, tint = DangerRed, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Sign Out", color = DangerRed, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Clear session and return to login", color = TextSecondary, fontSize = 12.sp)
                    }
                    Icon(Icons.Outlined.ChevronRight, null, tint = DangerRed.copy(alpha = 0.5f), modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

// ── Profile Card ───────────────────────────────────────────────────────────

@Composable
private fun ProfileCard(
    sessionManager: UserSessionManager,
    displayName: String,
    onEditClick: () -> Unit
) {
    val email = sessionManager.getUserEmail() ?: ""
    val role  = sessionManager.getUserRole()
    val isAdmin = role == UserRole.ADMIN
    val roleLabel = if (isAdmin) "ADMIN" else "CITIZEN"
    val roleDot   = if (isAdmin) AdminAmber else StatusActive

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() },
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
                // Avatar with initials
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(SurfaceDarker, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displayName.take(2).uppercase().ifEmpty { "ZG" },
                        color = roleDot,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = displayName.ifEmpty { "Unknown User" },
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (email.isNotEmpty()) {
                        Text(email, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 1.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(6.dp).background(roleDot, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(roleLabel, color = roleDot, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Icon(Icons.Outlined.Edit, "Edit profile", tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
    }
}

// ── Account Info Row ───────────────────────────────────────────────────────

@Composable
private fun AccountInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = TextPrimary, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
    }
}

// ── Top Bar ────────────────────────────────────────────────────────────────

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
            Text("Settings", color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Outlined.Security, "Security", tint = TextPrimary, modifier = Modifier.size(24.dp))
        }
        HorizontalDivider(color = DividerColor)
    }
}

// ── Shared row composables ─────────────────────────────────────────────────

@Composable
private fun SettingsNavigationRow(title: String, subtitle: String, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(2.dp))
                Text(subtitle, color = TextSecondary, fontSize = 13.sp)
            }
            Icon(Icons.Outlined.ChevronRight, "Navigate", tint = TextSecondary, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, color = TextSecondary, fontSize = 13.sp)
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
fun ZeroGridSettingsScreen() = SettingsScreen()

@Preview(showBackground = true)
@Composable
fun ZeroGridSettingsPreview() {
    ZeroGridTheme { ZeroGridSettingsScreen() }
}