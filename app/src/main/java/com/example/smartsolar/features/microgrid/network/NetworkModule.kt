package com.example.smartsolar.features.microgrid.network

import android.os.Build
import com.example.smartsolar.features.auth.network.ProsumerApiService
import com.example.smartsolar.features.reservations.network.ReservationApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkModule {

    val isEmulator: Boolean = (
        Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MODEL.contains("sdk_gphone")
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.HARDWARE.contains("goldfish")
            || Build.HARDWARE.contains("ranchu")
            || Build.PRODUCT.contains("sdk")
            || Build.PRODUCT.contains("google_sdk")
            || Build.PRODUCT.contains("emulator")
            || Build.BOARD.lowercase().contains("goldfish")
    )

    // For emulators: 10.0.2.2 routes to the host machine.
    // For physical devices on the current Wi-Fi network: 10.171.18.86
    // Fallback host if plugged into USB (adb reverse): 127.0.0.1
    val PRIMARY_HOST = if (isEmulator) "10.0.2.2" else "10.171.18.86"
    val FALLBACK_HOST = "127.0.0.1"
    const val PORT = 5000

    val BASE_URL = "http://$PRIMARY_HOST:$PORT/api/"

    private val okHttpClient: okhttp3.OkHttpClient by lazy {
        okhttp3.OkHttpClient.Builder()
            .connectTimeout(3, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                try {
                    chain.proceed(request)
                } catch (e: Exception) {
                    val currentHost = request.url().host()
                    val targetHost = if (currentHost == PRIMARY_HOST) FALLBACK_HOST else PRIMARY_HOST
                    val newUrl = request.url().newBuilder().host(targetHost).build()
                    val newRequest = request.newBuilder().url(newUrl).build()
                    chain.proceed(newRequest)
                }
            }
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
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
