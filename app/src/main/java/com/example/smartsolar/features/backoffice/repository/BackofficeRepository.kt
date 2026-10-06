package com.example.smartsolar.features.backoffice.repository

import com.example.smartsolar.features.backoffice.models.BackofficeProsumer
import com.example.smartsolar.features.backoffice.models.BackofficeUser
import com.example.smartsolar.features.backoffice.network.BackofficeApiService

class BackofficeRepository(private val apiService: BackofficeApiService) {

    suspend fun getUsers(token: String): Result<List<BackofficeUser>> {
        return try {
            val users = apiService.getAllUsers(token)
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserStatus(token: String, id: String, status: String): Result<Unit> {
        return try {
            val response = apiService.updateUserStatus(token, id, mapOf("status" to status))
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update status (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProsumers(token: String): Result<List<BackofficeProsumer>> {
        return try {
            val prosumers = apiService.getAllProsumers(token)
            Result.success(prosumers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDeactivationRequests(token: String): Result<List<BackofficeProsumer>> {
        return try {
            val requests = apiService.getDeactivationRequests(token)
            Result.success(requests)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approveDeactivation(token: String, id: String): Result<Unit> {
        return try {
            val response = apiService.approveDeactivation(token, id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Approval failed (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectDeactivation(token: String, id: String): Result<Unit> {
        return try {
            val response = apiService.rejectDeactivation(token, id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Rejection failed (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
