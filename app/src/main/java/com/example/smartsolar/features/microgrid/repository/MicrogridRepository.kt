package com.example.smartsolar.features.microgrid.repository

import com.example.smartsolar.features.microgrid.local.StationDao
import com.example.smartsolar.features.microgrid.models.CreateSlotRequest
import com.example.smartsolar.features.microgrid.models.EnergySlot
import com.example.smartsolar.features.microgrid.models.Station
import com.example.smartsolar.features.microgrid.network.MicrogridApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MicrogridRepository(
    private val apiService: MicrogridApiService,
    private val stationDao: StationDao
) {
    suspend fun getStations(): Result<List<Station>> = withContext(Dispatchers.IO) {
        try {
            val stations = apiService.getStations()
            stationDao.clearStations()
            stationDao.insertStations(stations)
            Result.success(stations)
        } catch (e: Exception) {
            try {
                val localStations = stationDao.getAllStations()
                if (localStations.isNotEmpty()) {
                    Result.success(localStations)
                } else {
                    Result.failure(e)
                }
            } catch (cacheException: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getStationById(stationId: String): Result<Station> = withContext(Dispatchers.IO) {
        try {
            val station = apiService.getStationById(stationId)
            stationDao.insertStation(station)
            Result.success(station)
        } catch (e: Exception) {
            try {
                val localStation = stationDao.getStationById(stationId)
                if (localStation != null) {
                    Result.success(localStation)
                } else {
                    Result.failure(e)
                }
            } catch (cacheException: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getStationSlots(stationId: String, date: String? = null): Result<List<EnergySlot>> = withContext(Dispatchers.IO) {
        try {
            val slots = apiService.getStationSlots(stationId, date)
            Result.success(slots)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createSlot(stationId: String, request: CreateSlotRequest): Result<EnergySlot> = withContext(Dispatchers.IO) {
        try {
            val slot = apiService.createSlot(stationId, request)
            Result.success(slot)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateSlotStatus(slotId: String, status: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateSlotStatus(slotId, status)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update slot status: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateStationStatus(stationId: String, status: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateStationStatus(stationId, status)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to update station status: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
