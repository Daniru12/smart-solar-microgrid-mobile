package com.example.smartsolar.features.auth.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.smartsolar.features.auth.models.ProsumerProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserDatabaseHelper(context: Context) : 
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "UserDatabase.db"
        private const val TABLE_PROFILES = "profiles"

        private const val KEY_ID = "id"
        private const val KEY_USER_ID = "userId"
        private const val KEY_NIC = "nic"
        private const val KEY_FIRST_NAME = "firstName"
        private const val KEY_LAST_NAME = "lastName"
        private const val KEY_EMAIL = "email"
        private const val KEY_PHONE = "phone"
        private const val KEY_ADDRESS = "address"
        private const val KEY_STATUS = "status"
        private const val KEY_CREATED_AT = "createdAt"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createProfileTable = ("CREATE TABLE " + TABLE_PROFILES + "("
                + KEY_ID + " TEXT PRIMARY KEY,"
                + KEY_USER_ID + " TEXT,"
                + KEY_NIC + " TEXT,"
                + KEY_FIRST_NAME + " TEXT,"
                + KEY_LAST_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT,"
                + KEY_PHONE + " TEXT,"
                + KEY_ADDRESS + " TEXT,"
                + KEY_STATUS + " TEXT,"
                + KEY_CREATED_AT + " TEXT" + ")")
        db.execSQL(createProfileTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROFILES")
        onCreate(db)
    }

    suspend fun saveProfile(profile: ProsumerProfile) = withContext(Dispatchers.IO) {
        val db = this@UserDatabaseHelper.writableDatabase
        val values = ContentValues().apply {
            put(KEY_ID, profile.id)
            put(KEY_USER_ID, profile.userId)
            put(KEY_NIC, profile.nic)
            put(KEY_FIRST_NAME, profile.firstName)
            put(KEY_LAST_NAME, profile.lastName)
            put(KEY_EMAIL, profile.email)
            put(KEY_PHONE, profile.phoneNumber)
            put(KEY_ADDRESS, profile.address)
            put(KEY_STATUS, profile.status)
            put(KEY_CREATED_AT, profile.createdAt)
        }
        db.insertWithOnConflict(TABLE_PROFILES, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        Unit
    }

    suspend fun getProfile(): ProsumerProfile? = withContext(Dispatchers.IO) {
        val db = this@UserDatabaseHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_PROFILES LIMIT 1", null)
        var profile: ProsumerProfile? = null

        if (cursor.moveToFirst()) {
            profile = ProsumerProfile(
                id = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ID)),
                userId = cursor.getString(cursor.getColumnIndexOrThrow(KEY_USER_ID)),
                nic = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NIC)),
                firstName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_FIRST_NAME)),
                lastName = cursor.getString(cursor.getColumnIndexOrThrow(KEY_LAST_NAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(KEY_EMAIL)),
                phoneNumber = cursor.getString(cursor.getColumnIndexOrThrow(KEY_PHONE)),
                address = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ADDRESS)),
                status = cursor.getString(cursor.getColumnIndexOrThrow(KEY_STATUS)),
                createdAt = cursor.getString(cursor.getColumnIndexOrThrow(KEY_CREATED_AT))
            )
        }
        cursor.close()
        profile
    }

    suspend fun clearProfile() = withContext(Dispatchers.IO) {
        val db = this@UserDatabaseHelper.writableDatabase
        db.delete(TABLE_PROFILES, null, null)
        Unit
    }
}
