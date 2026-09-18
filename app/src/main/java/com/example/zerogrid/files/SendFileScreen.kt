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
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import com.example.zerogrid.mesh.engine.MeshEngine
import com.example.zerogrid.mesh.engine.MeshNode
import com.example.zerogrid.navigation.Screen
import com.example.zerogrid.navigation.ZeroGridBottomBar
import com.example.zerogrid.ui.theme.*

@Composable
fun SendFileScreen(onNavigate: (Screen) -> Unit = {}) {
    val context = LocalContext.current
    val meshEngine = MeshEngine.getInstance(context)
    val peers by meshEngine.connectedPeers.collectAsState()

    var selectedPermission by remember { mutableStateOf("Downloadable") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var selectedRecipientId by remember { mutableStateOf("") }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
        selectedFileName = uri?.lastPathSegment?.substringAfterLast('/') ?: "Selected Document"
    }

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
            ChooseFileCard(
                selectedFileName = selectedFileName,
                onBrowseClick = { filePickerLauncher.launch("*/*") }
            )
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
            RecipientSelectionList(
                peers = peers,
                selectedRecipientId = selectedRecipientId,
                onSelectRecipient = { selectedRecipientId = it }
            )
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
            TransferSummaryCard(
                fileName = if (selectedFileName.isNotEmpty()) selectedFileName else "No file chosen",
                recipient = if (selectedRecipientId.isNotEmpty()) {
                    peers.find { it.nodeId == selectedRecipientId }?.alias ?: "Node-${selectedRecipientId.takeLast(4)}"
                } else if (peers.isNotEmpty()) {
                    "Broadcast to all peers"
                } else {
                    "No peer selected"
                }
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Send File Button
            Button(
                onClick = { onNavigate(Screen.FILE_TRANSFER) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = StatusActive),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Transmit File Over Mesh",
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
private fun ChooseFileCard(
    selectedFileName: String,
    onBrowseClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, if (selectedFileName.isNotEmpty()) StatusActive else DividerColor)
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
                Icon(
                    imageVector = if (selectedFileName.isNotEmpty()) Icons.Outlined.CheckCircle else Icons.Outlined.FileUpload,
                    contentDescription = null,
                    tint = StatusActive,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (selectedFileName.isNotEmpty()) selectedFileName else "Choose a file",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (selectedFileName.isNotEmpty()) "File selected from device storage ready to transmit." else "Select a document, image, or file from this device.",
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onBrowseClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SurfaceDarker),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DividerColor)
            ) {
                Text(
                    text = if (selectedFileName.isNotEmpty()) "Change File" else "Browse Files",
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
private fun RecipientSelectionList(
    peers: List<MeshNode>,
    selectedRecipientId: String,
    onSelectRecipient: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (peers.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, DividerColor)
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(imageVector = Icons.Outlined.Devices, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(28.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "No mesh peers discovered", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Bring another ZeroGrid device nearby to transfer files offline.", color = TextSecondary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                    }
                }
            }
        } else {
            peers.forEach { peer ->
                val isSelected = peer.nodeId == selectedRecipientId
                Card(
                    onClick = { onSelectRecipient(peer.nodeId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (isSelected) StatusActive else DividerColor, RoundedCornerShape(12.dp)),
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
                                    .size(36.dp)
                                    .background(if (isSelected) StatusActive else SurfaceDarker, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = peer.alias.take(1).uppercase(),
                                    color = if (isSelected) Color.Black else TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = peer.alias, color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${if (peer.hopDistance == 1) "Direct Link" else "${peer.hopDistance} hops"}  •  ${peer.transportType}",
                                    color = if (isSelected) StatusActive else TextSecondary,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        if (isSelected) {
                            Icon(imageVector = Icons.Outlined.CheckCircle, contentDescription = null, tint = StatusActive, modifier = Modifier.size(20.dp))
                        }
                    }
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
private fun TransferSummaryCard(
    fileName: String = "No file chosen",
    recipient: String = "No peer selected"
) {
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
            SummaryRow(label = "File:", value = fileName)
            Spacer(modifier = Modifier.height(8.dp))
            SummaryRow(label = "Recipient:", value = recipient)
            Spacer(modifier = Modifier.height(8.dp))
            SummaryRow(label = "Protocol:", value = "Mesh Multi-Hop P2P")
            Spacer(modifier = Modifier.height(8.dp))
            SummaryRow(label = "Status:", value = if (fileName != "No file chosen") "Ready to Queue" else "Awaiting Selection")
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