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

    @SerializedName("capacity")
    val capacity: Double = 0.0,

    @SerializedName("availableCapacity")
    val capacityAvailable: Double = 0.0,

    @SerializedName("status")
    val status: String = "Available"
) {
    val effectiveCapacity: Double
        get() = if (capacity > 0) capacity else capacityAvailable

    val bookedCapacity: Double
        get() = (effectiveCapacity - capacityAvailable).coerceAtLeast(0.0)

    val remainingCapacity: Double
        get() = capacityAvailable

    val percentRemaining: Float
        get() = if (effectiveCapacity > 0) ((capacityAvailable / effectiveCapacity) * 100).toFloat().coerceIn(0f, 100f) else 100f
}
