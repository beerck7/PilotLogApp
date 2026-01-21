package com.example.pilotlog.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface AircraftDao {
    @Query("SELECT * FROM aircraft")
    fun getAll(): kotlinx.coroutines.flow.Flow<List<Aircraft>>

    @Query("SELECT * FROM aircraft WHERE registration = :reg LIMIT 1")
    suspend fun getByRegistration(reg: String): Aircraft?

    @Insert
    suspend fun insert(aircraft: Aircraft): Long

    @Update
    suspend fun update(aircraft: Aircraft)

    @Delete
    suspend fun delete(aircraft: Aircraft)

    @Query("DELETE FROM aircraft")
    suspend fun deleteAll()
}
