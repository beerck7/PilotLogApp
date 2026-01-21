package com.example.pilotlog.data.repository

import com.example.pilotlog.data.Aircraft
import com.example.pilotlog.data.AircraftDao
import kotlinx.coroutines.flow.Flow

class AircraftRepositoryImpl(private val aircraftDao: AircraftDao) : AircraftRepository {
    override fun getAllAircraft(): Flow<List<Aircraft>> = aircraftDao.getAll()
    
    override suspend fun getByRegistration(registration: String): Aircraft? = aircraftDao.getByRegistration(registration)
    
    override suspend fun insert(aircraft: Aircraft): Long = aircraftDao.insert(aircraft)
    
    override suspend fun update(aircraft: Aircraft) = aircraftDao.update(aircraft)
    
    override suspend fun delete(aircraft: Aircraft) = aircraftDao.delete(aircraft)
    
    override suspend fun deleteAll() = aircraftDao.deleteAll()
}
