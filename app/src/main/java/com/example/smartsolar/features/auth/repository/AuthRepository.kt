package com.example.smartsolar.features.auth.repository

import com.example.smartsolar.features.auth.models.AuthResponse
import com.example.smartsolar.features.auth.models.LoginRequest
import com.example.smartsolar.features.microgrid.network.MicrogridApiService

class AuthRepository(private val apiService: MicrogridApiService) {
    suspend fun login(request: LoginRequest): Result<AuthResponse> {
        return try {
            val response = apiService.login(request)
            if (response.success && response.data != null) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message ?: "Unknown error occurred"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
