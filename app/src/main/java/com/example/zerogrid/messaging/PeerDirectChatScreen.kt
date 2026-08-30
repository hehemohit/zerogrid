package com.example.zerogrid.messaging

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zerogrid.mesh.engine.MeshEngine
import com.example.zerogrid.navigation.Screen
import com.example.zerogrid.navigation.ZeroGridBottomBar
import com.example.zerogrid.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Per-peer direct message chat screen.
 * Shows persistent conversation history (survives reconnections) and live incoming messages.
 */
@Composable
fun PeerDirectChatScreen(
    peerId: String,
    onNavigate: (Screen) -> Unit = {}
) {
    val context = LocalContext.current
    val meshEngine = remember { MeshEngine.getInstance(context) }

    var messageText by remember { mutableStateOf("") }
    val conversations by meshEngine.conversations.collectAsState()
    val connectedPeers by meshEngine.connectedPeers.collectAsState()

    // Live conversation — updates from the StateFlow as new messages arrive/are sent
    val messages = conversations[peerId] ?: emptyList()

    // Resolve display name from connected peers
    val peer = connectedPeers.firstOrNull { it.nodeId == peerId }
    val displayName = peer?.alias ?: "Peer ${peerId.takeLast(6)}"
    val isOnline = peer != null
    val hopInfo = peer?.let { "${it.hopDistance} hop${if (it.hopDistance != 1) "s" else ""}" } ?: "Offline"

    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = DarkBackground,
        topBar = {
            PeerChatTopBar(
                displayName = displayName,
                isOnline = isOnline,
                hopInfo = hopInfo,
                onBackClick = { onNavigate(Screen.MESSAGES) }
            )
        },
        bottomBar = {
            Column {
                PeerChatInputBar(
                    messageText = messageText,
                    onValueChange = { messageText = it },
                    onSend = {
                        val text = messageText.trim()
                        if (text.isNotEmpty()) {
                            meshEngine.sendDirectMessage(peerId, text)
                            messageText = ""
                        }
                    },
                    enabled = true
                )
                ZeroGridBottomBar(currentScreen = Screen.MESSAGES, onNavigate = onNavigate)
            }
        }
    ) { paddingValues ->
        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(SurfaceDarker, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = displayName.first().uppercaseChar().toString(),
                            color = StatusActive,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = displayName,
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isOnline) "Connected via mesh • $hopInfo" else "Not currently in range",
                        color = if (isOnline) StatusActive else TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBackground),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(horizontal = 32.dp)
                    ) {
                        Text(
                            text = "No messages yet.\nSay hello to ${displayName}!",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(20.dp),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    if (msg.isMine) {
                        SentMessageBubble(msg)
                    } else {
                        ReceivedMessageBubble(msg, displayName)
                    }
                }
            }
        }
    }
}

@Composable
private fun PeerChatTopBar(
    displayName: String,
    isOnline: Boolean,
    hopInfo: String,
    onBackClick: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = StatusActive,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(SurfaceDarker, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayName.first().uppercaseChar().toString(),
                    color = StatusActive,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                // Online indicator dot
                if (isOnline) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFF4CAF50), CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayName,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (isOnline) "Online • $hopInfo" else "Last seen on mesh",
                    color = if (isOnline) Color(0xFF4CAF50) else TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = "Encrypted",
                    tint = StatusActive,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "E2E",
                    color = StatusActive,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
    }
}

@Composable
private fun SentMessageBubble(msg: StoredMessage) {
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(msg.timestamp)
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(Color(0xFF0D2B28), RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp))
                .padding(12.dp, 10.dp)
        ) {
            Column {
                Text(
                    text = msg.text,
                    color = StatusActive,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = timeStr,
                        color = StatusActive.copy(alpha = 0.6f),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Sent",
                        tint = StatusActive.copy(alpha = 0.6f),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceivedMessageBubble(msg: StoredMessage, senderName: String) {
    val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(msg.timestamp)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(SurfaceDarker, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = senderName.first().uppercaseChar().toString(),
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = senderName,
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .background(CardBackground, RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp))
                    .padding(12.dp, 10.dp)
            ) {
                Column {
                    Text(
                        text = msg.text,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = timeStr,
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        if (msg.hopCount > 0) {
                            Text(
                                text = "• ${msg.hopCount}↗",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PeerChatInputBar(
    messageText: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    enabled: Boolean
) {
    Column {
        HorizontalDivider(color = DividerColor, thickness = 1.dp)
        if (!enabled) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDarker)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.WifiOff,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Device not in range — messages will send when reconnected",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBackground)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        text = if (enabled) "Send a message..." else "Device offline",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = CardBackground,
                    unfocusedContainerColor = CardBackground,
                    disabledContainerColor = SurfaceDarker,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                singleLine = true
            )
            Button(
                onClick = onSend,
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (enabled && messageText.isNotBlank()) StatusActive else SurfaceDarker,
                    disabledContainerColor = SurfaceDarker
                ),
                enabled = enabled && messageText.isNotBlank(),
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (enabled && messageText.isNotBlank()) Color.Black else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = Icons.Outlined.Lock, contentDescription = null, tint = StatusActive.copy(alpha = 0.5f), modifier = Modifier.size(11.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "End-to-end encrypted over mesh",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
