package com.example.smartsolar.features.microgrid.models

import com.google.gson.annotations.SerializedName

data class CreateSlotRequest(
    @SerializedName("date")
    val date: String,

    @SerializedName("startTime")
    val startTime: String,

    @SerializedName("endTime")
    val endTime: String,

    @SerializedName("capacity")
    val capacity: Double
)
