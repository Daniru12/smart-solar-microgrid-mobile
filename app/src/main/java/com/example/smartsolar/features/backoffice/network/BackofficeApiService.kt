package com.example.smartsolar.features.backoffice.network

import com.example.smartsolar.features.backoffice.models.BackofficeProsumer
import com.example.smartsolar.features.backoffice.models.BackofficeUser
import retrofit2.Response
import retrofit2.http.*

interface BackofficeApiService {

    // --- Users ---
    @GET("users")
    suspend fun getAllUsers(
        @Header("Authorization") token: String
    ): List<BackofficeUser>

    @PATCH("users/{id}/status")
    suspend fun updateUserStatus(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body body: Map<String, String>
    ): Response<Unit>

    // --- Prosumers ---
    @GET("prosumers")
    suspend fun getAllProsumers(
        @Header("Authorization") token: String
    ): List<BackofficeProsumer>

    @GET("prosumers/deactivation-requests")
    suspend fun getDeactivationRequests(
        @Header("Authorization") token: String
    ): List<BackofficeProsumer>

    @PATCH("prosumers/{id}/approve-deactivation")
    suspend fun approveDeactivation(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>

    @PATCH("prosumers/{id}/reject-deactivation")
    suspend fun rejectDeactivation(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>
}
