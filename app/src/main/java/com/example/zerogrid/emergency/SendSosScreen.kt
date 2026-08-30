package com.example.zerogrid.emergency

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zerogrid.mesh.engine.MeshEngine
import com.example.zerogrid.navigation.Screen
import com.example.zerogrid.ui.theme.*

@Composable
fun SendSosScreen(onNavigate: (Screen) -> Unit = {}) {
    val context = LocalContext.current
    val meshEngine = remember { MeshEngine.getInstance(context) }
    val connectedPeers by meshEngine.connectedPeers.collectAsState()

    var selectedType by remember { mutableStateOf("Medical Emergency") }
    var customDetails by remember { mutableStateOf("") }
    var shareLocation by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = { SendSosTopBar(onBackClick = { onNavigate(Screen.SOS_CENTER) }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Warning Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, AlertRedBorder, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF221114)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Outlined.Warning, contentDescription = null, tint = AlertPink, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Emergency broadcast will alert all ${connectedPeers.size} reachable peers in the mesh.",
                        color = AlertPink,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "SELECT EMERGENCY TYPE",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            val emergencyTypes = listOf(
                EmergencyTypeItem("Medical Emergency", "Injury, illness, or medical crisis", Icons.Outlined.MedicalServices),
                EmergencyTypeItem("Trapped / Assistance", "Cannot evacuate or need rescue", Icons.Outlined.Emergency),
                EmergencyTypeItem("Fire / Hazard", "Active fire, gas leak, or flood", Icons.Outlined.LocalFireDepartment),
                EmergencyTypeItem("Security / Threat", "Immediate security threat nearby", Icons.Outlined.Shield)
            )

            emergencyTypes.forEach { type ->
                EmergencyTypeRadioCard(
                    item = type,
                    isSelected = selectedType == type.title,
                    onClick = { selectedType = type.title }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "DETAILS (OPTIONAL)",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = customDetails,
                onValueChange = { customDetails = it },
                placeholder = { Text("Add specific location or details (e.g. 2nd floor, bleeding)...", color = TextSecondary, fontSize = 13.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground,
                    focusedBorderColor = StatusActive,
                    unfocusedBorderColor = DividerColor,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Include Location Toggle
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Attach GPS Coordinates", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Shares device coordinates with mesh peers", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = shareLocation,
                        onCheckedChange = { shareLocation = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.Black,
                            checkedTrackColor = StatusActive,
                            uncheckedThumbColor = TextSecondary,
                            uncheckedTrackColor = SurfaceDarker
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Broadcast SOS Button
            Button(
                onClick = {
                    meshEngine.triggerSosBeacon(
                        category = selectedType,
                        message = customDetails.ifBlank { "Emergency assistance required: $selectedType" },
                        lat = 0.0,
                        lon = 0.0
                    )
                    onNavigate(Screen.SOS_CENTER)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AlertPink),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Outlined.Campaign, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "BROADCAST SOS ALERT",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private data class EmergencyTypeItem(val title: String, val subtitle: String, val icon: ImageVector)

@Composable
private fun EmergencyTypeRadioCard(item: EmergencyTypeItem, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, if (isSelected) AlertPink else DividerColor, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF2B1418) else CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(if (isSelected) AlertPink else SurfaceDarker, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = if (isSelected) Color.White else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = item.subtitle, color = TextSecondary, fontSize = 12.sp)
            }
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = AlertPink, unselectedColor = TextSecondary)
            )
        }
    }
}

@Composable
private fun SendSosTopBar(onBackClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Broadcast Emergency SOS",
                color = AlertPink,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
    }
}