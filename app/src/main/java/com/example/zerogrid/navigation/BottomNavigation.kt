package com.example.zerogrid.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class NavTab(val label: String, val icon: ImageVector, val screen: Screen) {
    HOME("Home", Icons.Outlined.Home, Screen.HOME),
    MESSAGES("Messages", Icons.Outlined.ChatBubbleOutline, Screen.MESSAGES),
    MESH("Mesh", Icons.Outlined.Share, Screen.MESH),
    FILES("Files", Icons.Outlined.Folder, Screen.FILES),
    SETTINGS("Settings", Icons.Outlined.Settings, Screen.SETTINGS)
}

@Composable
fun ZeroGridBottomBar(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        tonalElevation = 4.dp
    ) {
        NavTab.entries.forEach { tab ->
            val selected = when (tab) {
                NavTab.HOME -> currentScreen == Screen.HOME
                NavTab.MESSAGES -> (currentScreen == Screen.MESSAGES || currentScreen == Screen.CHANNELS || currentScreen == Screen.CHAT_DETAIL || currentScreen == Screen.PEER_DIRECT_CHAT)
                NavTab.MESH -> (currentScreen == Screen.MESH || currentScreen == Screen.PEER_DETAILS || currentScreen == Screen.NETWORK_STATUS)
                NavTab.FILES -> (currentScreen == Screen.FILES || currentScreen == Screen.SEND_FILE || currentScreen == Screen.FILE_TRANSFER)
                NavTab.SETTINGS -> (currentScreen == Screen.SETTINGS || currentScreen == Screen.SECURITY_PRIVACY)
            }
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(tab.screen) },
                icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                label = { Text(text = tab.label, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
