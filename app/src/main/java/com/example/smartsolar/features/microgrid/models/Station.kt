package com.example.smartsolar.features.microgrid.models

import com.google.gson.annotations.SerializedName

data class Station(
    @SerializedName("stationId")
    val id: String,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("address")
    val address: String,
    
    @SerializedName("latitude")
    val latitude: Double,
    
    @SerializedName("longitude")
    val longitude: Double,
    
    @SerializedName("capacity")
    val capacityKw: Int,
    
    @SerializedName("availableStorage")
    val availableStorageKwh: Int,
    
    @SerializedName("status")
    val status: String // "Active" or "Inactive"
)
