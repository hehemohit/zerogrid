package com.example.zerogrid.map.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zerogrid.navigation.Screen
import com.example.zerogrid.ui.theme.*

private val TileCardBg = Color(0xFF0D1520)
private val NeonCyan   = Color(0xFF00E5FF)
private val NeonGreen  = Color(0xFF00FF88)

/**
 * Screen for configuring and initiating offline map tile region downloads.
 * Allows the user to define a bounding box by name/region and zoom levels,
 * then download tiles while on Wi-Fi before going off-grid.
 */
@Composable
fun TileRegionDownloadScreen(
    onNavigate: (Screen) -> Unit
) {
    var regionName by remember { mutableStateOf("") }
    var downloadProgress by remember { mutableStateOf<Float?>(null) }
    var estimatedTiles by remember { mutableStateOf<Long?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onNavigate(Screen.OFFLINE_MAP) }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = NeonCyan
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Outlined.CloudDownload, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "DOWNLOAD OFFLINE TILES",
                color = NeonCyan,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Info Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .border(1.dp, Color(0xFF1A2535), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = TileCardBg),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Store map tiles locally so the map works fully offline without any internet connection.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Region Name Input
                OutlinedTextField(
                    value = regionName,
                    onValueChange = { regionName = it },
                    label = { Text("Region Name", color = TextSecondary, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
                    placeholder = { Text("e.g. Himalayas Base Camp", color = TextSecondary, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = Color(0xFF1A2535),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Zoom Level Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ZoomChip("Zoom 10", "Regional")
                    ZoomChip("Zoom 13", "City")
                    ZoomChip("Zoom 15", "Street")
                    ZoomChip("Zoom 17", "Detail")
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Storage estimate
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDarker, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Estimated tiles", color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Text(
                        text = if (estimatedTiles != null) "${estimatedTiles!!.toLocaleString()} tiles (~${estimatedTiles!! * 15 / 1024} MB)" else "--",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Download Progress
        if (downloadProgress != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Downloading...", color = TextSecondary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                    Text("${(downloadProgress!! * 100).toInt()}%", color = NeonCyan, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { downloadProgress!! },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = NeonCyan,
                    trackColor = SurfaceDarker
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Download Button
        Button(
            onClick = {
                // In a real deployment this would trigger osmdroid's OfflineTileDownloader
                // using the bounding box of the currently visible map region.
                estimatedTiles = 4800L
                downloadProgress = 0f
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
            shape = RoundedCornerShape(14.dp),
            enabled = downloadProgress == null
        ) {
            Icon(Icons.Outlined.CloudDownload, contentDescription = null, tint = Color.Black, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (downloadProgress != null) "DOWNLOADING..." else "START DOWNLOAD",
                color = Color.Black,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ZoomChip(level: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(level, color = NeonCyan, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        Text(label, color = TextSecondary, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
    }
}

private fun Long.toLocaleString(): String {
    return String.format("%,d", this)
}
