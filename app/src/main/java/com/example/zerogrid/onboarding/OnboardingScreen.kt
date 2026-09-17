package com.example.zerogrid.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zerogrid.navigation.Screen
import com.example.zerogrid.ui.components.PlainLanguageInfoCard
import com.example.zerogrid.ui.components.ZeroGridButton
import com.example.zerogrid.ui.theme.MeshLineColor

@Composable
fun OnBoardingScreen(onNavigate: (Screen) -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ZeroGrid",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "1 • 3",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Titles
        Text(
            text = "Stay Connected When Cell Networks Fail",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "ZeroGrid connects nearby phones directly to each other without needing cellular service, cell towers, or Wi-Fi routers.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Hero Graphic Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(16.dp)
        ) {
            MeshGraphicCanvas()
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Plain Language Info Explanation
        PlainLanguageInfoCard(
            title = "What is a Mesh Network?",
            explanation = "Instead of talking through cell towers, phones talk directly to nearby phones. Messages 'hop' from phone to phone until reaching the receiver — extending coverage across crowds or disaster zones.",
            initiallyExpanded = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Features List
        FeatureItem(
            icon = Icons.Outlined.CloudOff,
            title = "Zero Internet Needed",
            description = "Works off-grid using your device's built-in Bluetooth and Wi-Fi Direct radio hardware."
        )

        Spacer(modifier = Modifier.height(12.dp))

        FeatureItem(
            icon = Icons.Outlined.Share,
            title = "Automatic Relay Hops",
            description = "Your message automatically relays through intermediate phones to reach people out of direct radio range."
        )

        Spacer(modifier = Modifier.height(12.dp))

        FeatureItem(
            icon = Icons.Outlined.Lock,
            title = "End-to-End Encrypted",
            description = "Intermediate relay phones cannot read your private messages or identity."
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Bottom Actions
        ZeroGridButton(
            text = "Continue to Permissions",
            onClick = { onNavigate(Screen.PERMISSIONS) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = { /* Learn more action */ }) {
            Text(
                text = "How relay hops work under stress",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun FeatureItem(icon: ImageVector, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun MeshGraphicCanvas() {
    val primaryColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = Modifier.fillMaxSize()) {
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

        // Define node positions
        val node1 = Offset(size.width * 0.2f, size.height * 0.5f)
        val node2 = Offset(size.width * 0.4f, size.height * 0.3f)
        val node3 = Offset(size.width * 0.6f, size.height * 0.6f)
        val node4 = Offset(size.width * 0.8f, size.height * 0.4f)
        val node5 = Offset(size.width * 0.5f, size.height * 0.8f)

        // Draw connections
        val lines = listOf(
            node1 to node2, node2 to node3, node3 to node4,
            node1 to node5, node5 to node3
        )

        for ((start, end) in lines) {
            drawLine(
                color = MeshLineColor,
                start = start,
                end = end,
                strokeWidth = 3f,
                pathEffect = dashEffect
            )
        }

        // Draw node devices
        val nodes = listOf(node1, node2, node3, node4, node5)
        for (node in nodes) {
            val rectSize = Size(36f, 60f)
            drawRoundRect(
                color = primaryColor,
                topLeft = Offset(node.x - (rectSize.width / 2), node.y - (rectSize.height / 2)),
                size = rectSize,
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = 3f)
            )
            drawCircle(
                color = primaryColor,
                radius = 4f,
                center = node
            )
        }
    }
}
