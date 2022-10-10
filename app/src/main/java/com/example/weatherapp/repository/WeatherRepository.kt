package com.example.weatherapp.repository

import com.example.weatherapp.api.RetrofitInstance

class WeatherRepository {
    suspend fun getWeather(apiKey: String, city: String, days: Int, aqi: String, alerts: String) =
        RetrofitInstance.api.getForecast(apiKey, city, days, aqi, alerts)
}