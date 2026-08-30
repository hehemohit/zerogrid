package com.example.zerogrid.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.*
import com.example.zerogrid.emergency.*
import com.example.zerogrid.files.*
import com.example.zerogrid.home.*
import com.example.zerogrid.mesh.*
import com.example.zerogrid.messaging.*
import com.example.zerogrid.onboarding.*
import com.example.zerogrid.settings.*

@Composable
fun ZeroGridApp() {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    val backStack = remember { mutableStateListOf<Screen>() }

    fun navigateTo(screen: Screen) {
        if (currentScreen != screen) {
            backStack.add(currentScreen)
            currentScreen = screen
        }
    }

    fun navigateBack() {
        if (backStack.isNotEmpty()) {
            currentScreen = backStack.removeAt(backStack.size - 1)
        } else if (currentScreen != Screen.HOME) {
            currentScreen = Screen.HOME
        }
    }

    BackHandler(enabled = (currentScreen != Screen.HOME || backStack.isNotEmpty())) {
        navigateBack()
    }

    when (currentScreen) {
        Screen.HOME -> MeshDashboardScreen(onNavigate = { navigateTo(it) })
        Screen.MESSAGES -> MessagesScreen(onNavigate = { navigateTo(it) })
        Screen.MESH -> NearbyDevicesScreen(onNavigate = { navigateTo(it) })
        Screen.FILES -> FilesScreen(onNavigate = { navigateTo(it) })
        Screen.SETTINGS -> SettingsScreen(onNavigate = { navigateTo(it) })
        Screen.SOS_CENTER -> SosCenterScreen(onNavigate = { navigateTo(it) })
        Screen.SEND_SOS -> SendSosScreen(onNavigate = { navigateTo(it) })
        Screen.SEND_FILE -> SendFileScreen(onNavigate = { navigateTo(it) })
        Screen.FILE_TRANSFER -> FileTransferScreen(onNavigate = { navigateTo(it) })
        Screen.PEER_DETAILS -> PeerDetailsScreen(onNavigate = { navigateTo(it) })
        Screen.CHANNELS -> ChannelsScreen(onNavigate = { navigateTo(it) })
        Screen.CHAT_DETAIL -> ChatDetailScreen(onNavigate = { navigateTo(it) })
        Screen.SPLASH -> SplashScreen(onNavigate = { navigateTo(it) })
        Screen.ONBOARDING -> OnBoardingScreen(onNavigate = { navigateTo(it) })
        Screen.PERMISSIONS -> PermissionsScreen(onNavigate = { navigateTo(it) })
        Screen.CREATE_IDENTITY -> CreateIdentityScreen(onNavigate = { navigateTo(it) })
        Screen.NETWORK_STATUS -> NetworkStatusScreen(onNavigate = { navigateTo(it) })
        Screen.SECURITY_PRIVACY -> SecurityPrivacyScreen(onNavigate = { navigateTo(it) })
    }
}
