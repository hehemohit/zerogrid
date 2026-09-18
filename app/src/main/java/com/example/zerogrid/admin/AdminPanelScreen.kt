package com.example.zerogrid.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zerogrid.hardware.HardwareRequirementBanner
import com.example.zerogrid.ui.theme.*
import com.zerogrid.mesh.app.ui.UserSessionManager

private val AdminAmber = Color(0xFFFF9500)
private val AdminRed   = Color(0xFFFF3B30)
private val AdminGreen = Color(0xFF30D158)

@Composable
fun AdminPanelScreen(
    sessionManager: UserSessionManager,
    onOpenMeshApp: () -> Unit,
    onLogout: () -> Unit
) {
    val displayName = sessionManager.getUserName().ifEmpty { "Admin" }
    val email       = sessionManager.getUserEmail() ?: ""
    val userId      = sessionManager.getUserId()?.takeLast(8) ?: "--------"

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            AdminTopBar(
                displayName = displayName,
                onLogout = {
                    sessionManager.clearSession()
                    onLogout()
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            HardwareRequirementBanner()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

            // ── Profile Header Card ────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar with initials
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                Brush.linearGradient(listOf(AdminAmber.copy(0.3f), AdminRed.copy(0.2f))),
                                CircleShape
                            )
                            .border(1.5.dp, AdminAmber, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayName.take(2).uppercase(),
                            color = AdminAmber,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(displayName, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        if (email.isNotEmpty()) {
                            Text(email, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        // ADMIN badge
                        Box(
                            modifier = Modifier
                                .background(AdminAmber.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .border(1.dp, AdminAmber.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "⚙  ADMIN",
                                color = AdminAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }

                HorizontalDivider(color = DividerColor)

                // ID row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Server ID", color = TextSecondary, fontSize = 12.sp)
                    Text(
                        "••••$userId",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Section Label ──────────────────────────────────────────────
            Text(
                "ADMIN MODULES",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            // ── Module Grid ────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminModuleCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Group,
                    title = "User Management",
                    description = "Approve accounts, manage roles"
                )
                AdminModuleCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Warning,
                    title = "SOS Live Feed",
                    description = "Monitor active emergency alerts"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminModuleCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.BarChart,
                    title = "Analytics",
                    description = "Network usage & activity reports"
                )
                AdminModuleCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Settings,
                    title = "System Config",
                    description = "Server and mesh settings"
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── Mesh App Access ────────────────────────────────────────────
            Text(
                "MESH ACCESS",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenMeshApp() },
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
                            .size(40.dp)
                            .background(PrimaryCyan.copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Router, null, tint = PrimaryCyan, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Open Mesh App", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("Access BLE/Wi-Fi mesh network", color = TextSecondary, fontSize = 12.sp)
                    }
                    Icon(Icons.Outlined.ChevronRight, null, tint = TextSecondary, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

// ── Top Bar ────────────────────────────────────────────────────────────────

@Composable
private fun AdminTopBar(displayName: String, onLogout: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Admin Panel", color = TextPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("Welcome back, $displayName", color = TextSecondary, fontSize = 13.sp)
            }
            IconButton(
                onClick = onLogout,
                modifier = Modifier
                    .size(40.dp)
                    .background(SurfaceDarker, RoundedCornerShape(10.dp))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                    contentDescription = "Sign out",
                    tint = AdminRed,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        HorizontalDivider(color = DividerColor)
    }
}

// ── Module Card ────────────────────────────────────────────────────────────

@Composable
private fun AdminModuleCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SurfaceDarker, RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, lineHeight = 16.sp)

            Spacer(modifier = Modifier.height(4.dp))

            Text(description, color = TextSecondary, fontSize = 11.sp, lineHeight = 14.sp)

            Spacer(modifier = Modifier.height(12.dp))

            // Coming Soon chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(AdminAmber.copy(alpha = 0.12f))
                    .border(1.dp, AdminAmber.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    "COMING SOON",
                    color = AdminAmber,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
