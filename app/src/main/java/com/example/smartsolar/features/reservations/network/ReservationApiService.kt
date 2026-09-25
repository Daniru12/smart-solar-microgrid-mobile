package com.example.smartsolar.features.reservations.network

import com.example.smartsolar.features.reservations.models.CreateReservationRequest
import com.example.smartsolar.features.reservations.models.Reservation
import com.example.smartsolar.features.reservations.models.UpdateReservationRequest
import retrofit2.http.*

data class ApiResponse<T>(val success: Boolean, val data: T?, val message: String?)

interface ReservationApiService {

    @GET("reservations/prosumer/{nic}")
    suspend fun getByProsumerNic(
        @Header("Authorization") token: String,
        @Path("nic") nic: String
    ): ApiResponse<List<Reservation>>

    @GET("reservations/prosumer/{nic}/search")
    suspend fun searchByProsumer(
        @Header("Authorization") token: String,
        @Path("nic") nic: String,
        @Query("status") status: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): ApiResponse<List<Reservation>>

    @GET("reservations/{id}")
    suspend fun getById(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): ApiResponse<Reservation>

    @GET("reservations")
    suspend fun getAll(
        @Header("Authorization") token: String
    ): ApiResponse<List<Reservation>>

    @GET("reservations/pending")
    suspend fun getPending(
        @Header("Authorization") token: String
    ): ApiResponse<List<Reservation>>

    @GET("reservations/dashboard")
    suspend fun getDashboardSummary(
        @Header("Authorization") token: String
    ): ApiResponse<DashboardSummary>

    @POST("reservations")
    suspend fun create(
        @Header("Authorization") token: String,
        @Body request: CreateReservationRequest
    ): ApiResponse<Reservation>

    @PUT("reservations/{id}")
    suspend fun update(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: UpdateReservationRequest
    ): ApiResponse<Reservation>

    @PUT("reservations/{id}/cancel")
    suspend fun cancel(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): ApiResponse<Reservation>

    @PUT("reservations/{id}/approve")
    suspend fun approve(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): ApiResponse<Reservation>
}

data class DashboardSummary(
    val pendingCount: Long,
    val approvedFutureCount: Long
)
