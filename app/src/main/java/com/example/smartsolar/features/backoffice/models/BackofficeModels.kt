package com.example.smartsolar.features.backoffice.models

import com.google.gson.annotations.SerializedName

data class BackofficeUser(
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("email")
    val email: String = "",
    
    @SerializedName("role")
    val role: String = "",
    
    @SerializedName("status")
    val status: String = "Active",
    
    @SerializedName("createdAt")
    val createdAt: String? = null
)

data class BackofficeProsumer(
    @SerializedName("id")
    val id: String = "",
    
    @SerializedName("userId")
    val userId: String = "",
    
    @SerializedName("nic")
    val nic: String = "",
    
    @SerializedName("name")
    val name: String = "",
    
    @SerializedName("address")
    val address: String = "",
    
    @SerializedName("status")
    val status: String = "Active",
    
    @SerializedName("deactivationRequested")
    val deactivationRequested: Boolean = false,
    
    @SerializedName("deactivationReason")
    val deactivationReason: String? = null
)
