package com.example.zerogrid.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zerogrid.ui.theme.SosRed
import com.example.zerogrid.ui.theme.SosRedDark
import kotlinx.coroutines.launch

@Composable
fun PanicWipeControl(
    onConfirmWipe: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    var holdProgress by remember { mutableStateOf(0f) }
    val animatedProgress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxWidth()) {
        // Explanatory Microcopy
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SosRed.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                .padding(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = SosRed,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "What happens: Pressing & holding for 3 seconds prepares to erase all encryption keys, identity data, and message history instantly.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Hold-for-3-seconds Button Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(SosRedDark)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            val job = scope.launch {
                                animatedProgress.animateTo(
                                    targetValue = 1f,
                                    animationSpec = tween(durationMillis = 3000, easing = LinearEasing)
                                ) {
                                    holdProgress = value
                                }
                                if (holdProgress >= 0.98f) {
                                    showDialog = true
                                }
                            }
                            tryAwaitRelease()
                            job.cancel()
                            scope.launch {
                                animatedProgress.snapTo(0f)
                                holdProgress = 0f
                            }
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            // Fill progress bar background
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(holdProgress)
                    .background(SosRed)
            )

            // Button label & icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.DeleteForever,
                    contentDescription = "Panic Wipe",
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (holdProgress > 0f) "Hold to Wipe (${(3 - (holdProgress * 3)).toInt() + 1}s)..." else "HOLD 3 SECONDS TO PANIC-WIPE",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.ui.graphics.Color.White
                    )
                    Text(
                        text = "Press and hold firm until gauge completes",
                        fontSize = 11.sp,
                        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }

    // Confirmation Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = "Confirm Emergency Panic-Wipe?",
                    fontWeight = FontWeight.Bold,
                    color = SosRed
                )
            },
            text = {
                Text(
                    text = "This will PERMANENTLY delete your cryptographic identity, messages, and device logs immediately. This action cannot be undone.",
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                ZeroGridButton(
                    text = "DESTROY ALL DATA",
                    onClick = {
                        showDialog = false
                        onConfirmWipe()
                    },
                    style = ZeroGridButtonStyle.EMERGENCY,
                    minHeight = 44.dp
                )
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("CANCEL", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
