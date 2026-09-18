package com.example.zerogrid.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zerogrid.network.AuthRepository
import com.example.zerogrid.network.AuthResult
import com.zerogrid.mesh.app.ui.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── UI State ───────────────────────────────────────────────────────────────

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val role: UserRole) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    data class AdminPending(val message: String) : AuthUiState()
}

// ── ViewModel ──────────────────────────────────────────────────────────────

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun resetState() {
        _uiState.value = AuthUiState.Idle
    }

    fun login(email: String, password: String) {
        if (_uiState.value is AuthUiState.Loading) return
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _uiState.value = when (val result = repository.login(email.trim(), password)) {
                is AuthResult.Success      -> AuthUiState.Success(result.role)
                is AuthResult.Error        -> AuthUiState.Error(result.message)
                is AuthResult.AdminPending -> AuthUiState.AdminPending(result.message)
            }
        }
    }

    fun register(email: String, password: String, displayName: String, role: UserRole) {
        if (_uiState.value is AuthUiState.Loading) return
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            _uiState.value = when (val result = repository.register(email.trim(), password, displayName.trim(), role)) {
                is AuthResult.Success      -> AuthUiState.Success(result.role)
                is AuthResult.Error        -> AuthUiState.Error(result.message)
                is AuthResult.AdminPending -> AuthUiState.AdminPending(result.message)
            }
        }
    }
}
