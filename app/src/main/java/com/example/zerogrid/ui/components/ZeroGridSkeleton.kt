package com.example.zerogrid.ui.components

import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ZeroGridSkeletonItem(
    modifier: Modifier = Modifier,
    height: Dp = 20.dp,
    width: Dp = Dp.Unspecified,
    cornerRadius: Dp = 8.dp
) {
    val transition = rememberInfiniteTransition(label = "skeleton_shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = InfiniteRepeatableSpec(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeleton_alpha"
    )

    val color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)

    val finalModifier = if (width != Dp.Unspecified) {
        modifier.width(width).height(height)
    } else {
        modifier.fillMaxWidth().height(height)
    }

    Box(
        modifier = finalModifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(color)
    )
}

@Composable
fun ZeroGridListSkeleton(
    itemCount: Int = 3,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        repeat(itemCount) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    ZeroGridSkeletonItem(height = 16.dp, width = 140.dp)
                    Spacer(modifier = Modifier.height(6.dp))
                    ZeroGridSkeletonItem(height = 12.dp, width = 200.dp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
