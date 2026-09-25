package com.example.smartsolar.features.auth.network

import com.example.smartsolar.features.auth.models.ProsumerProfile
import com.example.smartsolar.features.auth.models.RegisterRequest
import com.example.smartsolar.features.auth.models.UpdateProfileRequest
import retrofit2.http.*

data class ApiResponse<T>(val success: Boolean, val data: T?, val message: String?)

interface ProsumerApiService {

    @POST("prosumers/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<Any>

    @GET("prosumers/me")
    suspend fun getMe(
        @Header("Authorization") token: String
    ): ApiResponse<ProsumerProfile>

    @PUT("prosumers/me")
    suspend fun updateMe(
        @Header("Authorization") token: String,
        @Body request: UpdateProfileRequest
    ): ApiResponse<ProsumerProfile>

    @POST("prosumers/me/request-deactivation")
    suspend fun requestDeactivation(
        @Header("Authorization") token: String
    ): ApiResponse<Any>
}
