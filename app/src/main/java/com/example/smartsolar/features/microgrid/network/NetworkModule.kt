package com.example.smartsolar.features.microgrid.network

import com.example.smartsolar.features.auth.network.ProsumerApiService
import com.example.smartsolar.features.reservations.network.ReservationApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    
    private const val BASE_URL = "http://172.28.24.128:5059/api/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val microgridApiService: MicrogridApiService by lazy {
        retrofit.create(MicrogridApiService::class.java)
    }

    val reservationApiService: ReservationApiService by lazy {
        retrofit.create(ReservationApiService::class.java)
    }

    val prosumerApiService: ProsumerApiService by lazy {
        retrofit.create(ProsumerApiService::class.java)
    }

    // Auth token stored in memory during the session
    var authToken: String = ""
    fun bearerToken() = "Bearer $authToken"
}
