package com.zerogrid.mesh.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.LocalPolice
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zerogrid.mesh.engine.MeshEngine
import com.example.zerogrid.ui.theme.*

/**
 * Name Entry Screen.
 * Prompts user for a custom "Display Name" representing them on the mesh network,
 * then saves the identity and routes to the appropriate dashboard.
 */
@Composable
fun NameEntryScreen(
    isAuthority: Boolean,
    onNameConfirmed: (displayName: String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { UserSessionManager.getInstance(context) }
    val meshEngine = remember { MeshEngine.getInstance(context) }

    val accentColor = if (isAuthority) Color(0xFFFF9500) else StatusActive
    val roleTitle = if (isAuthority) "Authority / Rescuer" else "Citizen / Node User"
    val roleBadge = if (isAuthority) "AUTHORITY MODE" else "CITIZEN MODE"

    var displayName by remember {
        mutableStateOf(sessionManager.getUserName().ifEmpty { meshEngine.displayName.value })
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Identify Your Node",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Role Icon Header
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(accentColor.copy(alpha = 0.15f), CircleShape)
                        .border(1.5.dp, accentColor.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isAuthority) Icons.Outlined.Shield else Icons.Outlined.Person,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(34.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .background(accentColor.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                        .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = roleBadge,
                        color = accentColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Enter Your Display Name",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This name or callsign will represent your node to all peers and rescuers across the off-grid mesh.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Input Field
                OutlinedTextField(
                    value = displayName,
                    onValueChange = {
                        displayName = it
                        errorMessage = null
                    },
                    label = { Text("Display Name") },
                    placeholder = {
                        Text(if (isAuthority) "e.g. Officer Davis / Squad 4" else "e.g. Alex Node / Home")
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Badge,
                            contentDescription = null,
                            tint = accentColor
                        )
                    },
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = {
                        if (errorMessage != null) {
                            Text(text = errorMessage!!, color = AlertPink, fontSize = 11.sp)
                        } else {
                            Text(text = "Node ID: ${meshEngine.localNodeId}", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accentColor,
                        unfocusedBorderColor = DividerColor,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedLabelColor = accentColor,
                        unfocusedLabelColor = TextSecondary,
                        focusedContainerColor = CardBackground,
                        unfocusedContainerColor = CardBackground
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Informational Notice Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDarker),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isAuthority) Icons.Outlined.LocalPolice else Icons.Outlined.Person,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isAuthority) {
                                "Authority mode grants access to priority disaster alerts, responder telemetry, and incident command tools."
                            } else {
                                "Citizen mode grants access to local peer chat, multi-hop relaying, and emergency SOS broadcasting."
                            },
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Bottom Action Button
            Button(
                onClick = {
                    val trimmed = displayName.trim()
                    if (trimmed.isBlank()) {
                        errorMessage = "Please enter a valid display name"
                        return@Button
                    }
                    // Persist to session and mesh engine
                    sessionManager.setUserName(trimmed)
                    sessionManager.setUserRole(if (isAuthority) UserRole.AUTHORITY else UserRole.CITIZEN)
                    meshEngine.setDisplayName(trimmed)

                    onNameConfirmed(trimmed)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
            ) {
                Text(
                    text = "CONFIRM & ENTER DASHBOARD",
                    color = Color.Black,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
