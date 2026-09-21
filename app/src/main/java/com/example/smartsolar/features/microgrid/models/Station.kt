package com.example.smartsolar.features.microgrid.models

import com.google.gson.annotations.SerializedName

data class Station(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("address")
    val address: String,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double,
    
    @SerializedName("capacityKw")
    val capacityKw: Int,
    
    @SerializedName("availableStorageKwh")
    val availableStorageKwh: Int,
    
    @SerializedName("status")
    val status: String // "Active" or "Inactive"
)
