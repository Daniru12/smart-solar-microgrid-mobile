package com.example.smartsolar.features.auth.repository

import com.example.smartsolar.features.auth.models.ProsumerProfile
import com.example.smartsolar.features.auth.models.RegisterRequest
import com.example.smartsolar.features.auth.models.UpdateProfileRequest
import com.example.smartsolar.features.auth.network.ProsumerApiService

import com.example.smartsolar.features.auth.local.UserDatabaseHelper

class ProsumerRepository(private val api: ProsumerApiService, private val dbHelper: UserDatabaseHelper) {

    suspend fun register(request: RegisterRequest): Boolean {
        val response = api.register(request)
        return response.success
    }

    suspend fun getProfile(token: String): ProsumerProfile? {
        return try {
            val remoteProfile = api.getMe(token).data
            if (remoteProfile != null) {
                dbHelper.saveProfile(remoteProfile)
            }
            remoteProfile
        } catch (e: Exception) {
            dbHelper.getProfile() // Fallback to local cache if network fails
        }
    }

    suspend fun updateProfile(token: String, request: UpdateProfileRequest): ProsumerProfile? {
        val updated = api.updateMe(token, request).data
        if (updated != null) {
            dbHelper.saveProfile(updated)
        }
        return updated
    }

    suspend fun requestDeactivation(token: String): Boolean {
        return api.requestDeactivation(token).success
    }
}
