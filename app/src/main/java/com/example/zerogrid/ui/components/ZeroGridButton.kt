package com.example.zerogrid.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zerogrid.ui.theme.SosRed

enum class ZeroGridButtonStyle {
    PRIMARY,
    SECONDARY,
    EMERGENCY,
    OUTLINE
}

@Composable
fun ZeroGridButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ZeroGridButtonStyle = ZeroGridButtonStyle.PRIMARY,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    minHeight: Dp = 52.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.97f else 1.0f, label = "button_scale")

    val containerColor = when (style) {
        ZeroGridButtonStyle.PRIMARY -> MaterialTheme.colorScheme.primary
        ZeroGridButtonStyle.SECONDARY -> MaterialTheme.colorScheme.surfaceVariant
        ZeroGridButtonStyle.EMERGENCY -> SosRed
        ZeroGridButtonStyle.OUTLINE -> Color.Transparent
    }

    val contentColor = when (style) {
        ZeroGridButtonStyle.PRIMARY -> MaterialTheme.colorScheme.onPrimary
        ZeroGridButtonStyle.SECONDARY -> MaterialTheme.colorScheme.onSurface
        ZeroGridButtonStyle.EMERGENCY -> Color.White
        ZeroGridButtonStyle.OUTLINE -> MaterialTheme.colorScheme.primary
    }

    if (style == ZeroGridButtonStyle.OUTLINE) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = minHeight)
                .scale(scale),
            enabled = enabled && !isLoading,
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            interactionSource = interactionSource
        ) {
            ButtonContent(text = text, icon = icon, isLoading = isLoading, contentColor = contentColor)
        }
    } else {
        Button(
            onClick = onClick,
            modifier = modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = minHeight)
                .scale(scale),
            enabled = enabled && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor,
                disabledContainerColor = containerColor.copy(alpha = 0.4f),
                disabledContentColor = contentColor.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            interactionSource = interactionSource
        ) {
            ButtonContent(text = text, icon = icon, isLoading = isLoading, contentColor = contentColor)
        }
    }
}

@Composable
private fun ButtonContent(
    text: String,
    icon: ImageVector?,
    isLoading: Boolean,
    contentColor: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .height(20.dp)
                    .width(20.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(10.dp))
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.height(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}
