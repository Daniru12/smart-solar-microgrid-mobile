package com.example.smartsolar.api

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class Station(
    val stationId: String,
    val name: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val capacity: Double,
    val batteryCapacity: Double,
    val availableStorage: Int,
    val openingTime: String,
    val closingTime: String,
    val status: String
)

data class EnergySlot(
    val slotId: String,
    val stationId: String,
    val date: String,
    val startTime: String,
    val endTime: String,
    val capacity: Double,
    val availableCapacity: Double,
    val status: String
)

class MicrogridApiClient {
    private val BASE_URL = "http://10.0.2.2:5059/api" // Assuming emulator connects to localhost

    suspend fun getActiveStations(): List<Station> = withContext(Dispatchers.IO) {
        val stations = mutableListOf<Station>()
        try {
            val url = URL("$BASE_URL/stations?status=active")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                val jsonArray = JSONArray(response)
                
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    stations.add(
                        Station(
                            obj.getString("stationId"),
                            obj.getString("name"),
                            obj.getString("address"),
                            obj.getDouble("latitude"),
                            obj.getDouble("longitude"),
                            obj.getDouble("capacity"),
                            obj.getDouble("batteryCapacity"),
                            obj.getInt("availableStorage"),
                            obj.getString("openingTime"),
                            obj.getString("closingTime"),
                            obj.getString("status")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        stations
    }

    suspend fun getAvailableSlots(stationId: String, date: String? = null): List<EnergySlot> = withContext(Dispatchers.IO) {
        val slots = mutableListOf<EnergySlot>()
        try {
            var urlString = "$BASE_URL/stations/$stationId/slots?status=available"
            if (date != null) {
                urlString += "&date=$date"
            }
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                val jsonArray = JSONArray(response)
                
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    slots.add(
                        EnergySlot(
                            obj.getString("slotId"),
                            obj.getString("stationId"),
                            obj.getString("date"),
                            obj.getString("startTime"),
                            obj.getString("endTime"),
                            obj.getDouble("capacity"),
                            obj.getDouble("availableCapacity"),
                            obj.getString("status")
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        slots
    }
}
