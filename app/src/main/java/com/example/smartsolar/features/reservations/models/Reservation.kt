package com.example.smartsolar.features.reservations.models

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Reservation(
    @SerializedName("id") val id: String,
    @SerializedName("prosumerNic") val prosumerNic: String,
    @SerializedName("stationId") val stationId: String,
    @SerializedName("stationName") val stationName: String,
    @SerializedName("slotId") val slotId: String,
    @SerializedName("reservationDate") val reservationDate: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("energyAmountKwh") val energyAmountKwh: Double,
    @SerializedName("status") val status: String,
    @SerializedName("notes") val notes: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("updatedAt") val updatedAt: String,
    @SerializedName("cancelledAt") val cancelledAt: String?,
    @SerializedName("approvedAt") val approvedAt: String?,
    @SerializedName("completedAt") val completedAt: String?
)

data class CreateReservationRequest(
    @SerializedName("prosumerNic") val prosumerNic: String,
    @SerializedName("stationId") val stationId: String,
    @SerializedName("slotId") val slotId: String,
    @SerializedName("reservationDate") val reservationDate: String,
    @SerializedName("energyAmountKwh") val energyAmountKwh: Double,
    @SerializedName("notes") val notes: String? = null
)

data class UpdateReservationRequest(
    @SerializedName("reservationDate") val reservationDate: String,
    @SerializedName("slotId") val slotId: String,
    @SerializedName("energyAmountKwh") val energyAmountKwh: Double,
    @SerializedName("notes") val notes: String? = null
)
