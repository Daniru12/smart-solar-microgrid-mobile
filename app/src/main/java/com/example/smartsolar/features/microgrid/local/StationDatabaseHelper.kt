package com.example.smartsolar.features.microgrid.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.smartsolar.features.microgrid.models.Station
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class StationDatabaseHelper(context: Context) : 
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION), StationDao {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "StationDatabase.db"
        private const val TABLE_STATIONS = "stations"

        private const val KEY_ID = "id"
        private const val KEY_NAME = "name"
        private const val KEY_ADDRESS = "address"
        private const val KEY_LAT = "latitude"
        private const val KEY_LNG = "longitude"
        private const val KEY_CAPACITY = "capacityKw"
        private const val KEY_STORAGE = "availableStorageKwh"
        private const val KEY_STATUS = "status"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createStationsTable = ("CREATE TABLE " + TABLE_STATIONS + "("
                + KEY_ID + " TEXT PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_ADDRESS + " TEXT,"
                + KEY_LAT + " REAL,"
                + KEY_LNG + " REAL,"
                + KEY_CAPACITY + " INTEGER,"
                + KEY_STORAGE + " INTEGER,"
                + KEY_STATUS + " TEXT" + ")")
        db.execSQL(createStationsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_STATIONS")
        onCreate(db)
    }

    override suspend fun getAllStations(): List<Station> = withContext(Dispatchers.IO) {
        val stationList = mutableListOf<Station>()
        val selectQuery = "SELECT  * FROM $TABLE_STATIONS"
        val db = this@StationDatabaseHelper.readableDatabase
        val cursor = db.rawQuery(selectQuery, null)

        if (cursor.moveToFirst()) {
            do {
                val station = Station(
                    id = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)),
                    name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)),
                    address = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ADDRESS)),
                    latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LAT)),
                    longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LNG)),
                    capacityKw = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CAPACITY)),
                    availableStorageKwh = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_STORAGE)),
                    status = cursor.getString(cursor.getColumnIndexOrThrow(KEY_STATUS))
                )
                stationList.add(station)
            } while (cursor.moveToNext())
        }
        cursor.close()
        stationList
    }

    override suspend fun getStationById(stationId: String): Station? = withContext(Dispatchers.IO) {
        val db = this@StationDatabaseHelper.readableDatabase
        val cursor = db.query(
            TABLE_STATIONS,
            arrayOf(KEY_ID, KEY_NAME, KEY_ADDRESS, KEY_LAT, KEY_LNG, KEY_CAPACITY, KEY_STORAGE, KEY_STATUS),
            "$KEY_ID=?",
            arrayOf(stationId),
            null, null, null, null
        )

        var station: Station? = null
        if (cursor.moveToFirst()) {
            station = Station(
                id = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)),
                name = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NAME)),
                address = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ADDRESS)),
                latitude = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LAT)),
                longitude = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_LNG)),
                capacityKw = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_CAPACITY)),
                availableStorageKwh = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_STORAGE)),
                status = cursor.getString(cursor.getColumnIndexOrThrow(KEY_STATUS))
            )
        }
        cursor.close()
        station
    }

    override suspend fun insertStations(stations: List<Station>) = withContext(Dispatchers.IO) {
        val db = this@StationDatabaseHelper.writableDatabase
        db.beginTransaction()
        try {
            for (station in stations) {
                val values = ContentValues().apply {
                    put(KEY_ID, station.id)
                    put(KEY_NAME, station.name)
                    put(KEY_ADDRESS, station.address)
                    put(KEY_LAT, station.latitude)
                    put(KEY_LNG, station.longitude)
                    put(KEY_CAPACITY, station.capacityKw)
                    put(KEY_STORAGE, station.availableStorageKwh)
                    put(KEY_STATUS, station.status)
                }
                db.insertWithOnConflict(TABLE_STATIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override suspend fun insertStation(station: Station) = withContext(Dispatchers.IO) {
        val db = this@StationDatabaseHelper.writableDatabase
        val values = ContentValues().apply {
            put(KEY_ID, station.id)
            put(KEY_NAME, station.name)
            put(KEY_ADDRESS, station.address)
            put(KEY_LAT, station.latitude)
            put(KEY_LNG, station.longitude)
            put(KEY_CAPACITY, station.capacityKw)
            put(KEY_STORAGE, station.availableStorageKwh)
            put(KEY_STATUS, station.status)
        }
        db.insertWithOnConflict(TABLE_STATIONS, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        Unit
    }

    override suspend fun clearStations() = withContext(Dispatchers.IO) {
        val db = this@StationDatabaseHelper.writableDatabase
        db.delete(TABLE_STATIONS, null, null)
        Unit
    }
}
