package com.example.pilotlog.data.repository

import com.example.pilotlog.data.Flight
import com.example.pilotlog.data.FlightWithAircraft
import kotlinx.coroutines.flow.Flow

interface FlightRepository {
    fun getAllFlights(): Flow<List<FlightWithAircraft>>
    fun searchFlights(query: String): Flow<List<FlightWithAircraft>>
    suspend fun getFlightById(id: Int): Flight?
    
    suspend fun insert(flight: Flight)
    suspend fun update(flight: Flight)
    suspend fun delete(flight: Flight)
    suspend fun deleteAll()
    
    fun getTotalFlightTime(): Flow<Int?>
    fun getFlightCount(): Flow<Int>
    fun getFlightTimeSince(since: Long): Flow<Int?>
}
