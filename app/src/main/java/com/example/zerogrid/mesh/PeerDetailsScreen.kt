package com.example.zerogrid

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zerogrid.navigation.Screen
import com.example.zerogrid.navigation.ZeroGridBottomBar
import com.example.zerogrid.ui.theme.*

@Composable
fun PeerDetailsScreen(onNavigate: (Screen) -> Unit = {}) {
    var trustDevice by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = DarkBackground,
        topBar = { PeerDetailsTopBar(onBackClick = { onNavigate(Screen.MESH) }) },
        bottomBar = { ZeroGridBottomBar(currentScreen = Screen.MESH, onNavigate = onNavigate) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            PeerHeaderSection()
            Spacer(modifier = Modifier.height(24.dp))
            ActionButtonsRow()
            Spacer(modifier = Modifier.height(24.dp))
            ConnectionInfoCard()
            Spacer(modifier = Modifier.height(20.dp))
            RouteToPeerCard()
            Spacer(modifier = Modifier.height(20.dp))
            IdentitySecurityCard()
            Spacer(modifier = Modifier.height(16.dp))
            TrustDeviceCard(checked = trustDevice, onCheckedChange = { trustDevice = it })
            Spacer(modifier = Modifier.height(16.dp))
            MenuNavigationItem(icon = Icons.Outlined.Folder, title = "View Shared Files", titleColor = TextPrimary)
            Spacer(modifier = Modifier.height(8.dp))
            MenuNavigationItem(icon = Icons.Outlined.DeleteOutline, title = "Remove / Forget Device", titleColor = AlertPink, showIcon = false)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PeerDetailsTopBar(onBackClick: () -> Unit = {}) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = StatusActive,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Peer Details",
                    color = StatusActive,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = "More",
                tint = StatusActive,
                modifier = Modifier.size(24.dp)
            )
        }
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
    }
}

@Composable
private fun PeerHeaderSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(SurfaceDarker, CircleShape)
                .border(1.dp, StatusActive, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Router,
                contentDescription = "Device",
                tint = StatusActive,
                modifier = Modifier.size(36.dp)
            )
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(StatusActive, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Rescue Team",
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).background(StatusActive, CircleShape))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Connected • 2 hops away",
                color = StatusActive,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "ZeroGrid Device",
            color = TextSecondary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .background(Color(0xFF1A3B40), RoundedCornerShape(16.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = "Secure",
                tint = StatusActive,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "End-to-End Encrypted",
                color = StatusActive,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun ActionButtonsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = { },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = StatusActive, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Outlined.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Message", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
            onClick = { },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusActive),
            border = androidx.compose.foundation.BorderStroke(1.dp, StatusActive),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Outlined.Upload, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Send File", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ConnectionInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "CONNECTION",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            ConnectionRow("Connection", "Wi-Fi Direct")
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            ConnectionRow("Status", "Connected")
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            ConnectionSignalRow("Signal", "Good", 3)
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            ConnectionRow("Hop Count", "2 hops")
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            ConnectionRow("Next Hop", "Device-7A42")
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            ConnectionRow("Latency", "45 ms")
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            ConnectionRow("Last Seen", "Just now", valueColor = StatusActive)
        }
    }
}

@Composable
private fun ConnectionRow(label: String, value: String, valueColor: Color = TextPrimary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 14.sp)
        Text(text = value, color = valueColor, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ConnectionSignalRow(label: String, value: String, signalBars: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextSecondary, fontSize = 14.sp)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                for (i in 1..4) {
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(14.dp)
                            .background(
                                if (i <= signalBars) StatusActive else SurfaceDarker,
                                RoundedCornerShape(2.dp)
                            )
                    )
                }
            }
            Text(text = value, color = TextPrimary, fontSize = 14.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun RouteToPeerCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "ROUTE TO PEER",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Route Graph Visual
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(SurfaceDarker, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Outlined.PhoneAndroid, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                }
                Text(text = "You", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 4.dp))

                Box(modifier = Modifier.width(2.dp).height(24.dp).background(StatusActive))

                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(SurfaceDarker, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Outlined.Router, contentDescription = null, tint = StatusActive, modifier = Modifier.size(16.dp))
                }
                Text(text = "Device-7A42", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 4.dp))

                Box(modifier = Modifier.width(2.dp).height(24.dp).background(StatusActive))

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(SurfaceDarker, CircleShape)
                        .border(1.dp, StatusActive, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Outlined.Router, contentDescription = null, tint = StatusActive, modifier = Modifier.size(20.dp))
                }
                Text(text = "Rescue Team", color = StatusActive, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 4.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Traffic is forwarded through encrypted relay nodes.",
                color = TextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}

@Composable
private fun IdentitySecurityCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "IDENTITY & SECURITY",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Outlined.VerifiedUser, contentDescription = null, tint = StatusActive, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Peer Identity Verified", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Verified", color = StatusActive, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            ConnectionRow("Device ID", "ZG-7A42-••••")
            ConnectionRow("Fingerprint", "84:A7:••:••:21:F9")
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Verify this device's identity before sharing sensitive information.",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            OutlinedButton(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
            ) {
                Text(text = "Verify Identity", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun TrustDeviceCard(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Trust this device", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Allow ZeroGrid to recognize this peer as a trusted device.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = StatusActive,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = SurfaceDarker
                )
            )
        }
    }
}

@Composable
private fun MenuNavigationItem(icon: ImageVector, title: String, titleColor: Color, showIcon: Boolean = true) {
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = icon, contentDescription = null, tint = titleColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = title, color = titleColor, fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }
            if (showIcon) {
                Icon(imageVector = Icons.Outlined.ChevronRight, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun ZeroGridPeerDetailsScreen() = PeerDetailsScreen()

@Preview(showBackground = true)
@Composable
fun ZeroGridPeerDetailsPreview() {
    ZeroGridTheme {
        ZeroGridPeerDetailsScreen()
    }
}