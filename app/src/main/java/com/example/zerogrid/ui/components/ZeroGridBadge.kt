package com.example.zerogrid.ui.components

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zerogrid.ui.theme.StatusActive
import com.example.zerogrid.ui.theme.StatusOffline
import com.example.zerogrid.ui.theme.StatusSearching

enum class MeshConnectionState {
    CONNECTED,
    SEARCHING,
    OFFLINE
}

@Composable
fun ZeroGridBadge(
    state: MeshConnectionState,
    modifier: Modifier = Modifier,
    customText: String? = null,
    hopCount: Int? = null
) {
    val (dotColor, text, bgColor) = when (state) {
        MeshConnectionState.CONNECTED -> Triple(
            StatusActive,
            customText ?: if (hopCount != null && hopCount > 0) "Connected ($hopCount hops)" else "Connected (Direct)",
            StatusActive.copy(alpha = 0.15f)
        )
        MeshConnectionState.SEARCHING -> Triple(
            StatusSearching,
            customText ?: "Searching Mesh...",
            StatusSearching.copy(alpha = 0.15f)
        )
        MeshConnectionState.OFFLINE -> Triple(
            StatusOffline,
            customText ?: "Offline",
            StatusOffline.copy(alpha = 0.15f)
        )
    }

    val transition = rememberInfiniteTransition(label = "badge_pulse")
    val pulseScale by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(20.dp))
            .border(1.dp, dotColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(if (state == MeshConnectionState.SEARCHING || state == MeshConnectionState.CONNECTED) pulseScale else 1f)
                    .background(dotColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
