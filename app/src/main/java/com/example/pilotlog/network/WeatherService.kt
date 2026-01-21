package com.example.pilotlog.network

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface WeatherService {
    @GET("metar/{station}")
    suspend fun getMetar(
        @Path("station") station: String,
        @Header("X-API-Key") apiKey: String
    ): WeatherResponse

    @GET("taf/{station}")
    suspend fun getTaf(
        @Path("station") station: String,
        @Header("X-API-Key") apiKey: String
    ): WeatherResponse
}

data class WeatherResponse(
    val data: List<String>,
    val results: Int
)
