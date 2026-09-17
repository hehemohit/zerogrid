package com.example.zerogrid.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SettingsBluetooth
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zerogrid.navigation.Screen
import com.example.zerogrid.ui.components.ZeroGridButton
import com.example.zerogrid.ui.theme.GreenGranted
import com.example.zerogrid.ui.theme.RedNotGranted

@Composable
fun PermissionsScreen(onNavigate: (Screen) -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Top Logo and Heading
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = "Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ZeroGrid",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Enable Hardware Connectivity",
            color = MaterialTheme.colorScheme.primary,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "To send messages without cell service, ZeroGrid requires permission to use your phone's local Bluetooth and Wi-Fi antennas.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Custom Stepper Component
        ZeroGridStepper(currentStep = 2, totalSteps = 3)

        Spacer(modifier = Modifier.height(24.dp))

        // Permissions List
        PermissionItemCard(
            icon = Icons.Filled.SettingsBluetooth,
            title = "Nearby Devices (Bluetooth)",
            status = "Not granted",
            isGranted = false,
            description = "Discovers nearby mesh phones up to 100 meters away.",
            label = "REQUIRED",
            onAllowClick = { /* Handle Bluetooth permission */ }
        )

        Spacer(modifier = Modifier.height(12.dp))

        PermissionItemCard(
            icon = Icons.Filled.Wifi,
            title = "Wi-Fi Direct & LAN",
            status = "Not granted",
            isGranted = false,
            description = "High-speed multi-hop file transfers without internet.",
            label = "REQUIRED",
            onAllowClick = { /* Handle Wi-Fi permission */ }
        )

        Spacer(modifier = Modifier.height(12.dp))

        PermissionItemCard(
            icon = Icons.Filled.LocationOn,
            title = "Location Access",
            status = "Not granted",
            isGranted = false,
            description = "Android requires location permission to perform Bluetooth LE & Wi-Fi scanning.",
            label = "REQUIRED",
            onAllowClick = { /* Handle Location permission */ }
        )

        Spacer(modifier = Modifier.height(12.dp))

        PermissionItemCard(
            icon = Icons.Filled.Notifications,
            title = "Notifications",
            status = "Not granted",
            isGranted = false,
            description = "Receive emergency SOS alerts and mesh status updates in real-time.",
            label = "RECOMMENDED",
            onAllowClick = { /* Handle Notifications permission */ }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Info Box Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Info",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Why zero internet is safe & private",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "ZeroGrid uses local hardware only. Your phone will never upload location or personal data to remote servers.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Bottom Actions
        ZeroGridButton(
            text = "Continue to Create Identity",
            onClick = { onNavigate(Screen.CREATE_IDENTITY) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = { onNavigate(Screen.CREATE_IDENTITY) }) {
            Text(
                text = "Review permissions later in Settings",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun PermissionItemCard(
    icon: ImageVector,
    title: String,
    status: String,
    isGranted: Boolean,
    description: String,
    label: String,
    onAllowClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = status,
                            color = if (isGranted) GreenGranted else RedNotGranted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                FilledTonalButton(
                    onClick = onAllowClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(
                        text = "Allow Access",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun ZeroGridStepper(currentStep: Int, totalSteps: Int) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(modifier = Modifier.width(180.dp).height(10.dp)) {
            val totalLineWidth = size.width
            val totalLineHeight = size.height
            val segmentWidth = totalLineWidth / totalSteps
            val lineThickness = 4.dp.toPx()

            for (i in 0 until totalSteps) {
                val startX = segmentWidth * i
                val isActive = i < currentStep
                drawLine(
                    color = if (isActive) primaryColor else trackColor,
                    start = Offset(startX, totalLineHeight / 2),
                    end = Offset(startX + segmentWidth - 8f, totalLineHeight / 2),
                    strokeWidth = lineThickness
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "STEP $currentStep OF $totalSteps",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
