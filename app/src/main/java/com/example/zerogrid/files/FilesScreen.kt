package com.example.zerogrid.files

import androidx.compose.material.icons.automirrored.outlined.ArrowForward

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
fun FilesScreen(onNavigate: (Screen) -> Unit = {}) {
    Scaffold(
        containerColor = DarkBackground,
        topBar = { FilesTopBar() },
        bottomBar = { ZeroGridBottomBar(currentScreen = Screen.FILES, onNavigate = onNavigate) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate(Screen.SEND_FILE) },
                containerColor = StatusActive,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add File", modifier = Modifier.size(24.dp))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            MeshActiveStatusBanner()
            Spacer(modifier = Modifier.height(16.dp))

            // Local Storage Card
            LocalStorageCard()
            Spacer(modifier = Modifier.height(20.dp))

            // Active Transfers Section
            Text(
                text = "Active Transfers",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            ActiveTransfersSection()
            Spacer(modifier = Modifier.height(20.dp))

            // Files Shared With You Banner Card
            FilesSharedBannerCard()
            Spacer(modifier = Modifier.height(24.dp))

            // Recent Files Section
            Text(
                text = "Recent Files",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            RecentFilesSection()

            Spacer(modifier = Modifier.height(80.dp)) // Padding for FAB
        }
    }
}

@Composable
private fun FilesTopBar() {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Files",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = "Search",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Icon(
                    imageVector = Icons.Outlined.MoreVert,
                    contentDescription = "More Options",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
    }
}

@Composable
private fun MeshActiveStatusBanner() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(6.dp).background(StatusActive, CircleShape))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Mesh Active",
                color = StatusActive,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = "12 peers reachable",
            color = TextSecondary,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun LocalStorageCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Local Storage",
                color = TextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "2.4 GB used",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "5.6 GB available",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { 0.3f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = StatusActive,
                trackColor = SurfaceDarker,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

@Composable
private fun ActiveTransfersSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Transfer 1: Emergency-Map.pdf (Incoming)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DividerColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFF3B1A1E), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Outlined.PictureAsPdf, contentDescription = null, tint = AlertPink, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Emergency-Map.pdf",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "From Rescue Team",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(SurfaceDarker, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Outlined.Download, contentDescription = "Download", tint = StatusActive, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "12.4/18.2 MB (68%)",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "2.1 MB/s  •  2 hops",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { 0.68f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = StatusActive,
                    trackColor = SurfaceDarker,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }

        // Transfer 2: Medical-Supplies.jpg (Outgoing)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DividerColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(SurfaceDarker, RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Outlined.Image, contentDescription = null, tint = StatusActive, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Medical-Supplies.jpg",
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "To Device-7A42",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(SurfaceDarker, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Outlined.Upload, contentDescription = "Upload", tint = StatusActive, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "34%",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Direct connection",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { 0.34f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp),
                    color = StatusActive,
                    trackColor = SurfaceDarker,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun FilesSharedBannerCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, StatusActive, RoundedCornerShape(12.dp)),
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
            Column {
                Text(
                    text = "Files Shared With You",
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "3 active shares",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "View",
                    color = StatusActive,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = StatusActive,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun RecentFilesSection() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        RecentFileItem(
            icon = Icons.Outlined.PictureAsPdf,
            iconTint = AlertPink,
            iconBg = Color(0xFF3B1A1E),
            title = "Emergency-Map.pdf",
            details = "18.2 MB  •  Received  •  From Rescue Team",
            time = "12:38"
        )
        RecentFileItem(
            icon = Icons.Outlined.Image,
            iconTint = StatusActive,
            iconBg = SurfaceDarker,
            title = "Safe-Zone.jpg",
            details = "4.2 MB  •  Received  •  From Alex",
            time = "12:15"
        )
    }
}

@Composable
private fun RecentFileItem(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    details: String,
    time: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBg, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = details,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = time,
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}



@Composable
fun ZeroGridFilesScreen() = FilesScreen()

@Preview(showBackground = true)
@Composable
fun ZeroGridFilesPreview() {
    ZeroGridTheme {
        ZeroGridFilesScreen()
    }
}