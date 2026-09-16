package com.zerogrid.mesh.app.ui.navigation

/**
 * App navigation states for role selection, identity setup, and role-specific dashboards.
 */
sealed class AppScreen {
    object RoleSelection : AppScreen()
    data class NameEntry(val isAuthority: Boolean) : AppScreen()
    object UserDashboard : AppScreen()
    object AuthorityDashboard : AppScreen()
}
