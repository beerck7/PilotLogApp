package com.example.pilotlog.data.repository

import com.example.pilotlog.data.Aircraft
import kotlinx.coroutines.flow.Flow

interface AircraftRepository {
    fun getAllAircraft(): Flow<List<Aircraft>>
    suspend fun getByRegistration(registration: String): Aircraft?
    suspend fun insert(aircraft: Aircraft): Long
    suspend fun update(aircraft: Aircraft)
    suspend fun delete(aircraft: Aircraft)
    suspend fun deleteAll()
}
