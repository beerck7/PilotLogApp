package com.example.pilotlog.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.checkwx.com/"

    val instance: WeatherService by lazy {
        val client = okhttp3.OkHttpClient.Builder()
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(WeatherService::class.java)
    }

    val openSkyService: OpenSkyService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://opensky-network.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(OpenSkyService::class.java)
    }
}
