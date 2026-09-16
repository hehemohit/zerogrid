package com.zerogrid.mesh.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zerogrid.mesh.engine.MeshEngine
import com.example.zerogrid.ui.theme.*

/**
 * Authority Command Panel dashboard for government and rescue officials.
 * Provides placeholder emergency broadcast tools, command telemetry, and network monitoring.
 */
@Composable
fun AuthorityDashboardScreen(
    userName: String,
    onBackToRoles: () -> Unit,
    onOpenFullMeshApp: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val meshEngine = remember { MeshEngine.getInstance(context) }
    val connectedPeers by meshEngine.connectedPeers.collectAsState()
    val sosAlerts by meshEngine.sosAlerts.collectAsState()
    val isMeshActive by meshEngine.isMeshActive.collectAsState()

    val authorityAccent = Color(0xFFFF9500) // Amber / Command authority color

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackToRoles) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Switch Role",
                            tint = authorityAccent,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Authority Command Panel",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = if (userName.isNotBlank()) "Officer: $userName • Rescuer Mode" else "ZeroGrid Mesh • Official / Rescuer Mode",
                            color = authorityAccent,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    // Role Switch Chip
                    AssistChip(
                        onClick = onBackToRoles,
                        label = {
                            Text(
                                text = "Switch",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = authorityAccent
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.SwapHoriz,
                                contentDescription = null,
                                tint = authorityAccent,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = SurfaceDarker
                        ),
                        border = AssistChipDefaults.assistChipBorder(
                            enabled = true,
                            borderColor = authorityAccent.copy(alpha = 0.35f)
                        )
                    )
                }
                HorizontalDivider(color = DividerColor, thickness = 1.dp)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Authority Identity Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, authorityAccent.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(authorityAccent.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.LocalPolice,
                            contentDescription = null,
                            tint = authorityAccent,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (userName.isNotBlank()) userName else "Official / Rescuer",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Command Node: ${meshEngine.localNodeId}",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Box(
                        modifier = Modifier
                            .background(Color(0xFF33200B), RoundedCornerShape(6.dp))
                            .border(1.dp, authorityAccent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "AUTHORITY",
                            color = authorityAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Emergency Broadcast Tools Section (Required Placeholder Text)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF3B30).copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Campaign,
                                contentDescription = null,
                                tint = Color(0xFFFF3B30),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "EMERGENCY BROADCAST TOOLS",
                                color = Color(0xFFFF3B30),
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "PRIORITY 10 OVERRIDE",
                            color = authorityAccent,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "The Authority Command Panel grants authorized personnel high-priority mesh broadcast capabilities. Broadcast emergency evacuation orders, hazard alerts, search-and-rescue directives, and public safety announcements directly across all discovered nodes and relays.",
                        color = TextPrimary.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        lineHeight = 19.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Broadcast Tool Placeholders
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        AuthorityToolItem(
                            title = "High-Priority Disaster Alert",
                            description = "Overrides civilian muted notifications with high-decibel heads-up alarm on all mesh nodes in range.",
                            icon = Icons.Outlined.WarningAmber,
                            accentColor = Color(0xFFFF3B30)
                        )

                        AuthorityToolItem(
                            title = "Evacuation Sector Directive",
                            description = "Dispatches geo-referenced evacuation routes and safe zone coordinates over BLE & Wi-Fi Direct.",
                            icon = Icons.Outlined.Navigation,
                            accentColor = authorityAccent
                        )

                        AuthorityToolItem(
                            title = "Search & Rescue Responder Channel",
                            description = "Dedicated encrypted channel for inter-agency coordination, victim triage, and resource allocation.",
                            icon = Icons.Outlined.SpatialAudioOff,
                            accentColor = StatusActive
                        )
                    }
                }
            }

            // Command Telemetry & Network Overview
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "INCIDENT COMMAND TELEMETRY",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Monitored Peers", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text(
                                text = "${connectedPeers.size} active",
                                color = if (connectedPeers.isNotEmpty()) authorityAccent else TextSecondary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Column {
                            Text(text = "Active SOS Beacons", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text(
                                text = "${sosAlerts.size} reported",
                                color = if (sosAlerts.isNotEmpty()) Color(0xFFFF3B30) else Color(0xFF4CAF50),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Column {
                            Text(text = "Relay Status", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                            Text(
                                text = if (isMeshActive) "Online" else "Standby",
                                color = if (isMeshActive) Color(0xFF4CAF50) else Color(0xFFFFB74D),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Quick Actions & Navigation to Complete Mesh Suite
            if (onOpenFullMeshApp != null) {
                Button(
                    onClick = onOpenFullMeshApp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = authorityAccent)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Chat,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ACCESS MESH CHANNELS & EMERGENCY MAP",
                        color = Color.Black,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun AuthorityToolItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SurfaceDarker, RoundedCornerShape(10.dp))
            .border(1.dp, accentColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(accentColor.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}
