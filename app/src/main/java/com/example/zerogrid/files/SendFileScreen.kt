package com.example.zerogrid.files

import androidx.compose.foundation.BorderStroke
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

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zerogrid.navigation.Screen
import com.example.zerogrid.navigation.ZeroGridBottomBar
import com.example.zerogrid.ui.theme.*

@Composable
fun SendFileScreen(onNavigate: (Screen) -> Unit = {}) {
    var selectedPermission by remember { mutableStateOf("Downloadable") }

    Scaffold(
        containerColor = DarkBackground,
        topBar = { SendFileTopBar(onBackClick = { onNavigate(Screen.FILES) }) },
        bottomBar = { ZeroGridBottomBar(currentScreen = Screen.FILES, onNavigate = onNavigate) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            SendFileStepIndicator()
            Spacer(modifier = Modifier.height(20.dp))

            // Select File Section
            Text(
                text = "SELECT FILE",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            ChooseFileCard()
            Spacer(modifier = Modifier.height(12.dp))
            RecentFilesSelectionList()
            Spacer(modifier = Modifier.height(20.dp))

            // Send To Section
            Text(
                text = "SEND TO",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            RecipientSelectionList()
            Spacer(modifier = Modifier.height(20.dp))

            // File Permission Section
            Text(
                text = "FILE PERMISSION",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))
            FilePermissionRow(selected = selectedPermission, onSelected = { selectedPermission = it })
            Spacer(modifier = Modifier.height(20.dp))

            // Transfer Summary Card
            TransferSummaryCard()
            Spacer(modifier = Modifier.height(24.dp))

            // Send File Button
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusActive),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Send File",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            // Footer Status
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Encrypted  •  Peer-to-Peer  •  No Cloud",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SendFileTopBar(onBackClick: () -> Unit = {}) {
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
                    text = "Send File",
                    color = StatusActive,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Surface(
                color = SurfaceDarker,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, DividerColor)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Shield,
                        contentDescription = null,
                        tint = StatusActive,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ENCRYPTED",
                        color = StatusActive,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
    }
}

@Composable
private fun SendFileStepIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        StepIndicatorItem(step = "1", label = "File", active = true)
        HorizontalDivider(modifier = Modifier.width(40.dp), color = DividerColor, thickness = 1.dp)
        StepIndicatorItem(step = "2", label = "Recipient", active = false)
        HorizontalDivider(modifier = Modifier.width(40.dp), color = DividerColor, thickness = 1.dp)
        StepIndicatorItem(step = "3", label = "Send", active = false)
    }
}

@Composable
private fun StepIndicatorItem(step: String, label: String, active: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(if (active) StatusActive else SurfaceDarker, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                color = if (active) Color.Black else TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = if (active) StatusActive else TextSecondary,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun ChooseFileCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DividerColor, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(SurfaceDarker, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Outlined.FileUpload, contentDescription = null, tint = StatusActive, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Choose a file",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Select a document, image, video, or other file from this device.",
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarker),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DividerColor)
            ) {
                Text(
                    text = "Browse Files",
                    color = StatusActive,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun RecentFilesSelectionList() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Selected file item
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
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Outlined.PictureAsPdf, contentDescription = null, tint = AlertPink, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Emergency-Map.pdf", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "18.2 MB", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
                Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null, tint = StatusActive, modifier = Modifier.size(20.dp))
            }
        }

        // Unselected file item
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
                Icon(imageVector = Icons.Outlined.Image, contentDescription = null, tint = StatusActive, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Safe-Zone.jpg", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "4.8 MB", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun RecipientSelectionList() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Selected Recipient
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, StatusActive, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(StatusActive, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "R", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Rescue Team", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = "2 hops away  •  Good connection", color = StatusActive, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                    Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null, tint = StatusActive, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceDarker, RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(text = "Via Device-7A42", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }

        // Unselected Recipient
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CardBackground),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, DividerColor)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(SurfaceDarker, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "A", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Alex", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "Direct connection", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

@Composable
private fun FilePermissionRow(selected: String, onSelected: (String) -> Unit) {
    val permissions = listOf("View Only", "Downloadable")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        permissions.forEach { perm ->
            val isSelected = perm == selected
            Button(
                onClick = { onSelected(perm) },
                modifier = Modifier
                    .height(48.dp)
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) StatusActive else CardBackground,
                    contentColor = if (isSelected) Color.Black else TextSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                border = if (!isSelected) BorderStroke(1.dp, DividerColor) else null,
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (perm == "View Only") Icons.Outlined.Visibility else Icons.Outlined.Download,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = perm.uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun TransferSummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, DividerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Transfer Summary",
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            SummaryRow(label = "File:", value = "Emergency-Map.pdf")
            Spacer(modifier = Modifier.height(8.dp))
            SummaryRow(label = "Size:", value = "18.2 MB")
            Spacer(modifier = Modifier.height(8.dp))
            SummaryRow(label = "Recipient:", value = "Rescue Team")
            Spacer(modifier = Modifier.height(8.dp))
            SummaryRow(label = "Route:", value = "Up to 2 hops")
            Spacer(modifier = Modifier.height(8.dp))
            SummaryRow(label = "Permission:", value = "Downloadable")
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 13.sp)
        Text(text = value, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace)
    }
}



@Composable
fun ZeroGridSendFileScreen() = SendFileScreen()

@Preview(showBackground = true)
@Composable
fun ZeroGridSendFilePreview() {
    ZeroGridTheme {
        ZeroGridSendFileScreen()
    }
}