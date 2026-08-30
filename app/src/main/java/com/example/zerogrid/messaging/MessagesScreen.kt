package com.example.zerogrid.messaging

import androidx.compose.material.icons.automirrored.outlined.Chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
fun MessagesScreen(onNavigate: (Screen) -> Unit = {}) {
    var selectedFilter by remember { mutableStateOf("All") }

    Scaffold(
        containerColor = DarkBackground,
        topBar = { MessagesTopBar() },
        bottomBar = { ZeroGridBottomBar(currentScreen = Screen.MESSAGES, onNavigate = onNavigate) },
        floatingActionButton = { NewMessageFab() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            MeshActiveStatusBar()
            Spacer(modifier = Modifier.height(16.dp))
            MessageFilterChipsRow(selected = selectedFilter, onSelected = { selectedFilter = it })
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "CHANNELS",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            ChannelsSection()

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "DIRECT MESSAGES",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            DirectMessagesSection()

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun MessagesTopBar() {
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
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "Mesh",
                    tint = StatusActive,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "ZeroGrid",
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
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = "Edit",
                    tint = StatusActive,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
    }
}

@Composable
private fun MeshActiveStatusBar() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).background(StatusActive, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MESH ACTIVE  •  12 PEERS",
                    color = StatusActive,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Row(
                modifier = Modifier
                    .background(SurfaceDarker, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = StatusActive,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Encrypted",
                    color = StatusActive,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun MessageFilterChipsRow(selected: String, onSelected: (String) -> Unit) {
    val filters = listOf("All", "Private", "Channels")
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
private fun ChannelsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(SurfaceDarker, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "#", color = StatusActive, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(text = "mesh", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "General mesh communication", color = TextSecondary, fontSize = 13.sp)
                    }
                }
                Text(text = "18 peers", color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
            }
        }

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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF3B1A1E), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Outlined.Campaign, contentDescription = null, tint = AlertPink, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "SOS", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(modifier = Modifier.size(5.dp).background(AlertPink, CircleShape))
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "Emergency broadcasts", color = TextSecondary, fontSize = 13.sp)
                    }
                }
                Text(
                    text = "3 alerts",
                    color = AlertPink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .background(Color(0xFF3B1A1E), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun DirectMessagesSection() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        DirectMessageCard(
            icon = Icons.Outlined.MedicalServices,
            name = "Rescue Team",
            message = "Evacuation point confirmed...",
            time = "12:41 PM",
            badgeCount = "2",
            subBadge = "2 hops",
            isSecure = true
        )
        DirectMessageCard(
            icon = null,
            initial = "A",
            name = "Alex",
            message = "Are you at the shelter?",
            time = "12:37 PM",
            badgeCount = "1",
            subBadge = "Direct",
            isSecure = false
        )
        DirectMessageCard(
            icon = Icons.Outlined.LocalHospital,
            name = "Emergency Unit",
            message = "Medical supplies have arrived.",
            time = "12:29 PM",
            badgeCount = null,
            subBadge = "3 hops",
            isSecure = false
        )
    }
}

@Composable
private fun DirectMessageCard(
    icon: ImageVector?,
    initial: String? = null,
    name: String,
    message: String,
    time: String,
    badgeCount: String?,
    subBadge: String,
    isSecure: Boolean
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
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(SurfaceDarker, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (icon != null) {
                        Icon(imageVector = icon, contentDescription = null, tint = StatusActive, modifier = Modifier.size(22.dp))
                    } else if (initial != null) {
                        Text(text = initial, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    if (isSecure) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(StatusActive, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = name, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(text = time, color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = message,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = subBadge,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier
                                .background(SurfaceDarker, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (badgeCount != null) {
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(StatusActive, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount,
                        color = Color.Black,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun NewMessageFab() {
    FloatingActionButton(
        onClick = { /* New Message */ },
        containerColor = StatusActive,
        contentColor = Color.Black,
        shape = CircleShape,
        modifier = Modifier.size(64.dp)
    ) {
        Icon(imageVector = Icons.AutoMirrored.Outlined.Chat, contentDescription = "New Message", modifier = Modifier.size(28.dp))
    }
}



@Composable
fun ZeroGridMessagesScreen() = MessagesScreen()

@Preview(showBackground = true)
@Composable
fun ZeroGridMessagesPreview() {
    ZeroGridTheme {
        ZeroGridMessagesScreen()
    }
}