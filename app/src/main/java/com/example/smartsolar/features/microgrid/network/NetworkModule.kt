package com.example.smartsolar.features.microgrid.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {
    
    // Placeholder base URL for emulator pointing to local host
    // Will be updated when the actual backend is ready
    private const val BASE_URL = "http://10.0.2.2:5059/api/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val microgridApiService: MicrogridApiService by lazy {
        retrofit.create(MicrogridApiService::class.java)
    }
}
