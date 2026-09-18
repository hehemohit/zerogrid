package com.example.zerogrid.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

// ── Request bodies ─────────────────────────────────────────────────────────

data class RegisterRequest(
    val email: String,
    val password: String,
    val displayName: String,
    val role: String           // "CITIZEN" or "ADMIN"
)

data class LoginRequest(
    val email: String,
    val password: String
)

// ── Response bodies ────────────────────────────────────────────────────────

data class UserDto(
    val id: String,
    val email: String,
    val displayName: String,
    val role: String           // "CITIZEN" or "ADMIN"
)

data class AuthResponse(
    val token: String,
    val user: UserDto
)

data class ApiErrorBody(
    val message: String
)

// ── Retrofit interface ─────────────────────────────────────────────────────

interface AuthApiService {

    @POST(ApiConstants.REGISTER)
    suspend fun register(@Body body: RegisterRequest): Response<AuthResponse>

    @POST(ApiConstants.LOGIN)
    suspend fun login(@Body body: LoginRequest): Response<AuthResponse>
}
