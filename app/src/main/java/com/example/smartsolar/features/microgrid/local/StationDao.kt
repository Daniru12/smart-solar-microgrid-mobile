package com.example.smartsolar.features.microgrid.local

import com.example.smartsolar.features.microgrid.models.Station

interface StationDao {
    suspend fun getAllStations(): List<Station>
    suspend fun getStationById(stationId: String): Station?
    suspend fun insertStations(stations: List<Station>)
    suspend fun insertStation(station: Station)
    suspend fun clearStations()
}
