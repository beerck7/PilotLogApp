package com.example.pilotlog.data.repository

import com.example.pilotlog.data.Flight
import com.example.pilotlog.data.FlightDao
import com.example.pilotlog.data.FlightWithAircraft
import kotlinx.coroutines.flow.Flow

class FlightRepositoryImpl(private val flightDao: FlightDao) : FlightRepository {
    override fun getAllFlights(): Flow<List<FlightWithAircraft>> = flightDao.getAll()
    
    override fun searchFlights(query: String): Flow<List<FlightWithAircraft>> = flightDao.searchFlights(query)
    
    override suspend fun getFlightById(id: Int): Flight? = flightDao.getById(id)
    
    override suspend fun insert(flight: Flight) = flightDao.insert(flight)
    
    override suspend fun update(flight: Flight) = flightDao.update(flight)
    
    override suspend fun delete(flight: Flight) = flightDao.delete(flight)
    
    override suspend fun deleteAll() = flightDao.deleteAll()
    
    override fun getTotalFlightTime(): Flow<Int?> = flightDao.getTotalFlightTime()
    
    override fun getFlightCount(): Flow<Int> = flightDao.getFlightCountLive()
    
    override fun getFlightTimeSince(since: Long): Flow<Int?> = flightDao.getFlightTimeSince(since)
}
