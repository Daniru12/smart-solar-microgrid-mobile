package com.example.smartsolar.features.microgrid.network

import com.example.smartsolar.features.microgrid.models.EnergySlot
import com.example.smartsolar.features.microgrid.models.Station
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Path
import retrofit2.http.Query
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

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): AuthApiResponse
}
