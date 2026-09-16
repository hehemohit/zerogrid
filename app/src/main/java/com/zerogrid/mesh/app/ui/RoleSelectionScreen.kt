package com.zerogrid.mesh.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.CellTower
import androidx.compose.material.icons.outlined.LocalPolice
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.zerogrid.mesh.engine.MeshEngine
import com.example.zerogrid.ui.theme.*

/**
 * Gateway screen for role selection.
 * Presents "I am a Citizen / Node User" and "I am an Authority / Rescuer" options,
 * and prompts for the user's name before routing to their chosen dashboard.
 */
@Composable
fun RoleSelectionScreen(
    onRoleSelected: (role: UserRole, userName: String) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { UserSessionManager.getInstance(context) }
    val meshEngine = remember { MeshEngine.getInstance(context) }

    // Dialog state for collecting user name
    var pendingRole by remember { mutableStateOf<UserRole?>(null) }
    var inputName by remember { mutableStateOf(sessionManager.getUserName().ifEmpty { meshEngine.displayName.value }) }
    var nameError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // Top Icon / Badge
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF003840), Color(0xFF0A1E29))
                            ),
                            CircleShape
                        )
                        .border(1.5.dp, StatusActive.copy(alpha = 0.6f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CellTower,
                        contentDescription = "ZeroGrid Mesh",
                        tint = StatusActive,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "ZeroGrid Mesh",
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Select your operational mode",
                    color = StatusActive,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Choose how your device participates in the peer-to-peer off-grid network.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Role Selection Cards Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Option 1: Citizen / Node User
                RoleOptionCard(
                    title = "I am a Citizen / Node User",
                    subtitle = "Standard off-grid node for civilian communication, location beacons, peer messaging, and local SOS requests.",
                    icon = Icons.Outlined.Person,
                    accentColor = StatusActive,
                    badgeText = "REGULAR NODE",
                    onClick = {
                        pendingRole = UserRole.CITIZEN
                    }
                )

                // Option 2: Authority / Rescuer
                RoleOptionCard(
                    title = "I am an Authority / Rescuer",
                    subtitle = "Command panel with emergency broadcast override, responder coordination, disaster management, and telemetry.",
                    icon = Icons.Outlined.Shield,
                    accentColor = Color(0xFFFF9500),
                    badgeText = "AUTHORITY & RESCUE",
                    onClick = {
                        pendingRole = UserRole.AUTHORITY
                    }
                )
            }

            // Footer note
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.AccountCircle,
                    contentDescription = null,
                    tint = TextSecondary.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Role and name can be updated at any time in settings",
                    color = TextSecondary.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Name Entry Dialog triggered upon selecting either role
        if (pendingRole != null) {
            val selectedRole = pendingRole!!
            val roleTitle = if (selectedRole == UserRole.CITIZEN) "Citizen / Node User" else "Authority / Rescuer"

            Dialog(
                onDismissRequest = {
                    pendingRole = null
                    nameError = null
                },
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (selectedRole == UserRole.CITIZEN) StatusActive.copy(alpha = 0.5f) else Color(0xFFFF9500).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDarker),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    if (selectedRole == UserRole.CITIZEN) StatusActive.copy(alpha = 0.15f) else Color(0xFFFF9500).copy(alpha = 0.15f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (selectedRole == UserRole.CITIZEN) Icons.Outlined.Person else Icons.Outlined.LocalPolice,
                                contentDescription = null,
                                tint = if (selectedRole == UserRole.CITIZEN) StatusActive else Color(0xFFFF9500),
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Identify Your Node",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )

                        Text(
                            text = "Entering as $roleTitle",
                            color = if (selectedRole == UserRole.CITIZEN) StatusActive else Color(0xFFFF9500),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Please enter your name or operational callsign. This will identify your node to peers on the mesh network.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = inputName,
                            onValueChange = {
                                inputName = it
                                nameError = null
                            },
                            label = { Text("Your Name / Callsign") },
                            placeholder = { Text(if (selectedRole == UserRole.CITIZEN) "e.g. Alex Node" else "e.g. Officer Davis / Rescuer 1") },
                            singleLine = true,
                            isError = nameError != null,
                            supportingText = {
                                if (nameError != null) {
                                    Text(text = nameError!!, color = AlertPink, fontSize = 11.sp)
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (selectedRole == UserRole.CITIZEN) StatusActive else Color(0xFFFF9500),
                                unfocusedBorderColor = DividerColor,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedLabelColor = if (selectedRole == UserRole.CITIZEN) StatusActive else Color(0xFFFF9500),
                                unfocusedLabelColor = TextSecondary,
                                focusedContainerColor = CardBackground,
                                unfocusedContainerColor = CardBackground
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    pendingRole = null
                                    nameError = null
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                            ) {
                                Text("Cancel", fontSize = 13.sp)
                            }

                            Button(
                                onClick = {
                                    val trimmed = inputName.trim()
                                    if (trimmed.isEmpty()) {
                                        nameError = "Name cannot be blank"
                                        return@Button
                                    }
                                    // Save name and role
                                    sessionManager.setUserName(trimmed)
                                    sessionManager.setUserRole(selectedRole)
                                    meshEngine.setDisplayName(trimmed)

                                    val role = selectedRole
                                    pendingRole = null
                                    onRoleSelected(role, trimmed)
                                },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(46.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedRole == UserRole.CITIZEN) StatusActive else Color(0xFFFF9500)
                                )
                            ) {
                                Text(
                                    text = "Continue",
                                    color = Color.Black,
                                    fontSize = 13.sp,
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
private fun RoleOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    badgeText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(1.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
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
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(accentColor.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = badgeText,
                        color = accentColor,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Select",
                    tint = accentColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = title,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}
