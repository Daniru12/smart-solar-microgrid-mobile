package com.example.smartsolar.features.microgrid.network

import android.os.Build
import com.example.smartsolar.features.auth.network.ProsumerApiService
import com.example.smartsolar.features.reservations.network.ReservationApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    private val isEmulator: Boolean = (Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
            || "google_sdk" == Build.PRODUCT)

    val BASE_URL = if (isEmulator) "http://10.0.2.2:5000/api/" else "http://10.89.18.86:5000/api/"

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

    val backofficeApiService: com.example.smartsolar.features.backoffice.network.BackofficeApiService by lazy {
        retrofit.create(com.example.smartsolar.features.backoffice.network.BackofficeApiService::class.java)
    }

    var authToken: String = ""
    fun bearerToken() = "Bearer $authToken"
}
