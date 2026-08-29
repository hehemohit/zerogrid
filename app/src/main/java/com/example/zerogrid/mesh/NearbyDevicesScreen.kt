package com.example.zerogrid

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zerogrid.ui.theme.*

@Composable
fun NearbyDevicesScreen() {
    var selectedFilter by remember { mutableStateOf("All") }

    Scaffold(
        containerColor = DarkBackground,
        topBar = { NearbyTopBar() },
        bottomBar = { DashboardBottomNavActiveMesh() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            MeshDiscoveryCard()
            Spacer(modifier = Modifier.height(16.dp))
            RadarGraphicCard()
            Spacer(modifier = Modifier.height(16.dp))
            FilterChipsRow(selected = selectedFilter, onSelected = { selectedFilter = it })
            Spacer(modifier = Modifier.height(16.dp))
            DevicesListSection()
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NearbyTopBar() {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = StatusActive,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Nearby Devices",
                    color = StatusActive,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = StatusActive,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(20.dp))
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = "More",
                    tint = StatusActive,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
    }
}

@Composable
private fun MeshDiscoveryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).background(StatusActive, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Mesh Discovery Active",
                    color = StatusActive,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Scanning for nearby ZeroGrid devices...",
                color = TextSecondary,
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )
            HorizontalDivider(color = DividerColor, thickness = 1.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "DEVICES FOUND", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "12", color = StatusActive, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "PROTOCOL", color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Wi-Fi Direct", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RadarGraphicCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                drawCircle(color = SurfaceDarker, radius = 50.dp.toPx(), center = center)
                drawCircle(color = DividerColor, radius = 75.dp.toPx(), center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f))
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(SurfaceDarker, CircleShape)
                    .border(1.dp, StatusActive, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = "Device",
                    tint = StatusActive,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun FilterChipsRow(selected: String, onSelected: (String) -> Unit) {
    val filters = listOf("All", "Direct", "2 Hops", "Relay")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filters.forEach { filter ->
            val isSelected = filter == selected
            Button(
                onClick = { onSelected(filter) },
                modifier = Modifier
                    .height(36.dp)
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) StatusActive else CardBackground,
                    contentColor = if (isSelected) Color.Black else TextSecondary
                ),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = filter,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun DevicesListSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DeviceCard(
            icon = Icons.Outlined.CellTower,
            name = "Alex",
            status = "Direct • Strong",
            subStatus = "Last seen: 12 sec ago",
            actionText = "Connect",
            isActionOutlined = false
        )
        DeviceCard(
            icon = Icons.Outlined.AltRoute,
            name = "Rescue Team",
            status = "Hop count: 2 • Via Device-7A42",
            signalBars = 2,
            actionText = "Connect",
            isActionOutlined = false
        )
        DeviceCard(
            icon = Icons.Outlined.Share,
            name = "Device-7A42",
            status = "Direct • Relay enabled",
            signalBars = 4,
            actionText = "View",
            isActionOutlined = true
        )
        DeviceCard(
            icon = Icons.Outlined.Share,
            name = "Emergency Unit",
            status = "Hop count: 3 • Relay node",
            signalBars = 1,
            actionText = "Connect",
            isActionOutlined = false
        )
    }
}

@Composable
private fun DeviceCard(
    icon: ImageVector,
    name: String,
    status: String,
    subStatus: String? = null,
    signalBars: Int? = null,
    actionText: String,
    isActionOutlined: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = StatusActive,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = name, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = status, color = TextSecondary, fontSize = 13.sp, fontFamily = FontFamily.Monospace)

                    if (signalBars != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            for (i in 1..4) {
                                Box(
                                    modifier = Modifier
                                        .width(10.dp)
                                        .height(4.dp)
                                        .background(
                                            if (i <= signalBars) StatusActive else SurfaceDarker,
                                            RoundedCornerShape(2.dp)
                                        )
                                )
                            }
                        }
                    }

                    if (subStatus != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = subStatus, color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (isActionOutlined) {
                OutlinedButton(
                    onClick = { },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(text = actionText, fontSize = 13.sp)
                }
            } else {
                Button(
                    onClick = { },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TextPrimary, contentColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(text = actionText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DashboardBottomNavActiveMesh() {
    NavigationBar(
        containerColor = BottomNavBg,
        contentColor = TextSecondary,
        tonalElevation = 0.dp
    ) {
        val items = listOf(
            Triple("Home", Icons.Outlined.Home, false),
            Triple("Messages", Icons.Outlined.ChatBubbleOutline, false),
            Triple("Mesh", Icons.Outlined.Share, true),
            Triple("Files", Icons.Outlined.Folder, false),
            Triple("Settings", Icons.Outlined.Settings, false)
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
fun ZeroGridNearbyDevicesScreen() = NearbyDevicesScreen()

@Preview(showBackground = true)
@Composable
fun ZeroGridNearbyDevicesPreview() {
    ZeroGridTheme {
        ZeroGridNearbyDevicesScreen()
    }
}