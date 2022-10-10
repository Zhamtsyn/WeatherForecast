package com.example.weatherapp.api

import com.example.weatherapp.response.WeatherResponse
import com.example.weatherapp.util.Constants.Companion.API_KEY
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherAPI {
    @GET("forecast.json")
    suspend fun getForecast(
        @Query("key")
        apiKey: String = API_KEY,
        @Query("q")
        city: String = "",
        @Query("days")
        days: Int = 1,
        @Query("aqi")
        aqi: String = "no",
        @Query("alerts")
        alerts: String = "no",
        @Query("lang")
        lang: String = "uk"
    ): Response<WeatherResponse>
}