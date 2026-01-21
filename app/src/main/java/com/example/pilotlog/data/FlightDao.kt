package com.example.pilotlog.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface FlightDao {
    @androidx.room.Transaction
    @Query("SELECT * FROM flight ORDER BY date DESC")
    fun getAll(): kotlinx.coroutines.flow.Flow<List<FlightWithAircraft>>

    @Query("SELECT * FROM flight WHERE id = :id")
    suspend fun getById(id: Int): Flight?

    @androidx.room.Transaction
    @Query("SELECT * FROM flight WHERE departureCode LIKE '%' || :query || '%' OR arrivalCode LIKE '%' || :query || '%' OR remarks LIKE '%' || :query || '%' ORDER BY date DESC")
    fun searchFlights(query: String): kotlinx.coroutines.flow.Flow<List<FlightWithAircraft>>

    @Insert
    suspend fun insert(flight: Flight)

    @Update
    suspend fun update(flight: Flight)

    @Delete
    suspend fun delete(flight: Flight)
    
    @Query("SELECT SUM(durationMinutes) FROM flight")
    fun getTotalFlightTime(): kotlinx.coroutines.flow.Flow<Int?>

    @Query("SELECT COUNT(*) FROM flight")
    fun getFlightCountLive(): kotlinx.coroutines.flow.Flow<Int>

    @Query("SELECT SUM(durationMinutes) FROM flight WHERE date >= :since")
    fun getFlightTimeSince(since: Long): kotlinx.coroutines.flow.Flow<Int?>

    @Query("SELECT COUNT(*) FROM flight")
    suspend fun getCount(): Int

    @Query("DELETE FROM flight")
    suspend fun deleteAll()
}
