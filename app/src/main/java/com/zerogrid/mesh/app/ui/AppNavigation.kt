package com.zerogrid.mesh.app.ui

import android.content.Context
import android.content.SharedPreferences

/**
 * Navigation routes for role-selection and dashboard routing.
 */
enum class AppRoute {
    ROUTE_ROLE_SELECTION,
    ROUTE_USER_DASHBOARD,
    ROUTE_AUTHORITY_DASHBOARD
}

/**
 * Operational mode / role in the ZeroGrid network.
 */
enum class UserRole {
    CITIZEN,
    AUTHORITY
}

/**
 * User session & role persistence manager.
 * Stores selected role and username across app restarts.
 */
class UserSessionManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "zerogrid_role_session"
        private const val KEY_USER_NAME = "saved_user_name"
        private const val KEY_USER_ROLE = "saved_user_role"

        @Volatile
        private var INSTANCE: UserSessionManager? = null

        fun getInstance(context: Context): UserSessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UserSessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "") ?: ""
    }

    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name.trim()).apply()
    }

    fun getUserRole(): UserRole? {
        val roleStr = prefs.getString(KEY_USER_ROLE, null) ?: return null
        return try {
            UserRole.valueOf(roleStr)
        } catch (_: Exception) {
            null
        }
    }

    fun setUserRole(role: UserRole?) {
        if (role == null) {
            prefs.edit().remove(KEY_USER_ROLE).apply()
        } else {
            prefs.edit().putString(KEY_USER_ROLE, role.name).apply()
        }
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
