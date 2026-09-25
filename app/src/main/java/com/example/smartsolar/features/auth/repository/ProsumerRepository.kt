package com.example.smartsolar.features.auth.repository

import com.example.smartsolar.features.auth.models.ProsumerProfile
import com.example.smartsolar.features.auth.models.RegisterRequest
import com.example.smartsolar.features.auth.models.UpdateProfileRequest
import com.example.smartsolar.features.auth.network.ProsumerApiService

class ProsumerRepository(private val api: ProsumerApiService) {

    suspend fun register(request: RegisterRequest): Boolean {
        val response = api.register(request)
        return response.success
    }

    suspend fun getProfile(token: String): ProsumerProfile? {
        return api.getMe(token).data
    }

    suspend fun updateProfile(token: String, request: UpdateProfileRequest): ProsumerProfile? {
        return api.updateMe(token, request).data
    }

    suspend fun requestDeactivation(token: String): Boolean {
        return api.requestDeactivation(token).success
    }
}
