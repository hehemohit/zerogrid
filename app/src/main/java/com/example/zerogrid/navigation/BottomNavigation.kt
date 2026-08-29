package com.example.zerogrid.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zerogrid.ui.theme.BottomNavBg
import com.example.zerogrid.ui.theme.StatusActive
import com.example.zerogrid.ui.theme.TextSecondary

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
        containerColor = BottomNavBg,
        contentColor = TextSecondary,
        tonalElevation = 0.dp
    ) {
        NavTab.entries.forEach { tab ->
            val selected = when (tab) {
                NavTab.HOME -> currentScreen == Screen.HOME
                NavTab.MESSAGES -> currentScreen == Screen.MESSAGES || currentScreen == Screen.CHANNELS || currentScreen == Screen.CHAT_DETAIL
                NavTab.MESH -> currentScreen == Screen.MESH || currentScreen == Screen.PEER_DETAILS
                NavTab.FILES -> currentScreen == Screen.FILES || currentScreen == Screen.SEND_FILE || currentScreen == Screen.FILE_TRANSFER
                NavTab.SETTINGS -> currentScreen == Screen.SETTINGS
            }
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(tab.screen) },
                icon = { Icon(imageVector = tab.icon, contentDescription = tab.label) },
                label = { Text(text = tab.label, fontFamily = FontFamily.Monospace, fontSize = 10.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Black,
                    unselectedIconColor = TextSecondary,
                    selectedTextColor = TextSecondary,
                    unselectedTextColor = TextSecondary,
                    indicatorColor = StatusActive
                )
            )
        }
    }
}
