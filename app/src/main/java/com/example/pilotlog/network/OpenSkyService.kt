package com.example.pilotlog.network

import retrofit2.http.GET
import retrofit2.http.Query

data class OpenSkyResponse(
    val time: Int,
    val states: List<List<Any>>?
)

interface OpenSkyService {
    @GET("api/states/all")
    suspend fun getFlights(
        @Query("lamin") lamin: Double,
        @Query("lomin") lomin: Double,
        @Query("lamax") lamax: Double,
        @Query("lomax") lomax: Double
    ): OpenSkyResponse
}
