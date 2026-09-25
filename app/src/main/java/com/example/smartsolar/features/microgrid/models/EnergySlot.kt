package com.example.smartsolar.features.microgrid.models

import com.google.gson.annotations.SerializedName

data class EnergySlot(
    @SerializedName("slotId")
    val id: String,
    
    @SerializedName("stationId")
    val stationId: String,
    
    @SerializedName("date")
    val date: String,
    
    @SerializedName("startTime")
    val startTime: String,
    
    @SerializedName("endTime")
    val endTime: String,
    
    @SerializedName("availableCapacity")
    val capacityAvailable: Double,
    
    @SerializedName("status")
    val status: String // "Available", "Full", or "Inactive"
)
