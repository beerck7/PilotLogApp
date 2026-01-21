package com.example.pilotlog.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AirportDao {
    @Query("SELECT * FROM airport WHERE code = :code")
    suspend fun getByCode(code: String): Airport?

    @Query("SELECT * FROM airport ORDER BY code ASC")
    fun getAll(): LiveData<List<Airport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(airport: Airport)
}
