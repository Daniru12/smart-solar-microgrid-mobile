package com.example.smartsolar.features.microgrid.network

import com.example.smartsolar.features.microgrid.models.CreateSlotRequest
import com.example.smartsolar.features.microgrid.models.EnergySlot
import com.example.smartsolar.features.microgrid.models.Station
import retrofit2.Response
import retrofit2.http.*
import com.example.smartsolar.features.auth.models.LoginRequest
import com.example.smartsolar.features.auth.models.AuthApiResponse

interface MicrogridApiService {

    @GET("stations")
    suspend fun getStations(): List<Station>

    @GET("stations")
    suspend fun getActiveStations(@Query("status") status: String = "active"): List<Station>

    @GET("stations/{id}")
    suspend fun getStationById(@Path("id") id: String): Station

    @GET("stations/{id}/slots")
    suspend fun getStationSlots(
        @Path("id") id: String,
        @Query("date") date: String? = null
    ): List<EnergySlot>

    @POST("stations/{id}/slots")
    suspend fun createSlot(
        @Path("id") stationId: String,
        @Body request: CreateSlotRequest
    ): EnergySlot

    @PATCH("EnergySlots/{id}/status")
    suspend fun updateSlotStatus(
        @Path("id") slotId: String,
        @Body status: String
    ): Response<Unit>

    @PATCH("stations/{id}/status")
    suspend fun updateStationStatus(
        @Path("id") stationId: String,
        @Body status: String
    ): Response<Unit>

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): AuthApiResponse
}
