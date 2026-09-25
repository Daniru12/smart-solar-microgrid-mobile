package com.example.smartsolar.features.auth.models

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("token")
    val token: String,
    
    @SerializedName("userId")
    val userId: String,
    
    @SerializedName("role")
    val role: String,
    
    @SerializedName("email")
    val email: String? = null,

    @SerializedName("nic")
    val nic: String? = null,

    @SerializedName("name")
    val name: String? = null
)

data class AuthApiResponse(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String?,
    
    @SerializedName("data")
    val data: AuthResponse?
)
