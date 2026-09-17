package com.example.zerogrid.emergency

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
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DeviceHub
import androidx.compose.material.icons.outlined.Hub
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zerogrid.mesh.engine.MeshEngine
import com.example.zerogrid.mesh.engine.MeshPacket
import com.example.zerogrid.navigation.Screen
import com.example.zerogrid.navigation.ZeroGridBottomBar
import com.example.zerogrid.ui.components.MeshConnectionState
import com.example.zerogrid.ui.components.PlainLanguageInfoCard
import com.example.zerogrid.ui.components.ZeroGridBadge
import com.example.zerogrid.ui.components.ZeroGridButton
import com.example.zerogrid.ui.components.ZeroGridButtonStyle
import com.example.zerogrid.ui.theme.SosRed

@Composable
fun SosCenterScreen(onNavigate: (Screen) -> Unit = {}) {
    val meshEngine = MeshEngine.getInstance(LocalContext.current)
    val alerts by meshEngine.sosAlerts.collectAsState()
    val peers by meshEngine.connectedPeers.collectAsState()
    val acknowledgedIds by meshEngine.acknowledgedAlertIds.collectAsState()
    val localNodeId = meshEngine.localNodeId

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
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
            Spacer(modifier = Modifier.height(16.dp))
            ZeroGridBadge(
                state = if (peers.isNotEmpty()) MeshConnectionState.CONNECTED else MeshConnectionState.SEARCHING,
                customText = "Mesh Beacon Active (${peers.size} nodes in range)"
            )

            Spacer(modifier = Modifier.height(12.dp))
            PlainLanguageInfoCard(
                title = "What happens when you send SOS?",
                explanation = "Your high-contrast distress beacon immediately broadcasts across all surrounding devices up to 5 relay hops away.",
                initiallyExpanded = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Primary High-Contrast SOS Trigger
            ZeroGridButton(
                text = "TRIGGER EMERGENCY SOS BEACON",
                onClick = { onNavigate(Screen.SEND_SOS) },
                style = ZeroGridButtonStyle.EMERGENCY,
                icon = Icons.Outlined.Campaign,
                minHeight = 56.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "ACTIVE EMERGENCY DISTRESS ALERTS",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            ActiveAlertsSection(
                alerts = alerts,
                localNodeId = localNodeId,
                acknowledgedIds = acknowledgedIds,
                onAcknowledge = { packetId -> meshEngine.acknowledgeSosAlert(packetId) }
            )

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
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Emergency SOS Center",
                    color = SosRed,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
    }
}

@Composable
private fun ActiveAlertsSection(alerts: List<MeshPacket>, localNodeId: String, acknowledgedIds: Set<String>, onAcknowledge: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (alerts.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "No active emergency alerts", color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Surrounding mesh network is clear.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                    }
                }
            }
        } else {
            alerts.forEach { alert ->
                val isMine = alert.senderId == localNodeId
                val isAcknowledged = alert.packetId in acknowledgedIds

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SosRed.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isMine) "My SOS Beacon" else "Disaster Alert Received",
                                color = SosRed,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = alert.senderId,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = alert.payload,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )

                        if (!isMine && !isAcknowledged) {
                            Spacer(modifier = Modifier.height(10.dp))
                            ZeroGridButton(
                                text = "Acknowledge SOS",
                                onClick = { onAcknowledge(alert.packetId) },
                                style = ZeroGridButtonStyle.OUTLINE,
                                minHeight = 40.dp
                            )
                        }
                    }
                }
            }
        }
    }
}
