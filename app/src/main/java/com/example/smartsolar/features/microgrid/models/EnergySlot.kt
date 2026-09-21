package com.example.smartsolar.features.microgrid.models

import com.google.gson.annotations.SerializedName

data class EnergySlot(
    @SerializedName("id")
    val id: String,
    
    @SerializedName("stationId")
    val stationId: String,
    
    @SerializedName("date")
    val date: String,
    
    @SerializedName("startTime")
    val startTime: String,
    
    @SerializedName("endTime")
    val endTime: String,
    
    @SerializedName("capacityAvailable")
    val capacityAvailable: Int,
    
    @SerializedName("status")
    val status: String // "Available", "Full", or "Inactive"
)
