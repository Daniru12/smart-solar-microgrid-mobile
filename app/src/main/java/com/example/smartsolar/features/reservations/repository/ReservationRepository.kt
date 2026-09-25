package com.example.smartsolar.features.reservations.repository

import com.example.smartsolar.features.reservations.models.CreateReservationRequest
import com.example.smartsolar.features.reservations.models.Reservation
import com.example.smartsolar.features.reservations.models.UpdateReservationRequest
import com.example.smartsolar.features.reservations.network.DashboardSummary
import com.example.smartsolar.features.reservations.network.ReservationApiService

class ReservationRepository(private val api: ReservationApiService) {

    suspend fun getByNic(token: String, nic: String): List<Reservation> {
        return api.getByProsumerNic(token, nic).data ?: emptyList()
    }

    suspend fun searchByProsumer(
        token: String, nic: String, status: String? = null,
        from: String? = null, to: String? = null
    ): List<Reservation> {
        return api.searchByProsumer(token, nic, status, from, to).data ?: emptyList()
    }

    suspend fun getById(token: String, id: String): Reservation? {
        return api.getById(token, id).data
    }

    suspend fun getAll(token: String): List<Reservation> {
        return api.getAll(token).data ?: emptyList()
    }

    suspend fun getPending(token: String): List<Reservation> {
        return api.getPending(token).data ?: emptyList()
    }

    suspend fun getDashboardSummary(token: String): DashboardSummary? {
        return api.getDashboardSummary(token).data
    }

    suspend fun create(token: String, request: CreateReservationRequest): Reservation? {
        return api.create(token, request).data
    }

    suspend fun update(token: String, id: String, request: UpdateReservationRequest): Reservation? {
        return api.update(token, id, request).data
    }

    suspend fun cancel(token: String, id: String): Reservation? {
        return api.cancel(token, id).data
    }

    suspend fun approve(token: String, id: String): Reservation? {
        return api.approve(token, id).data
    }
}
