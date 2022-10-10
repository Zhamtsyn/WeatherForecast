package com.example.weatherapp.response

data class WeatherResponse(
    val location: Location,
    val current: Current,
    val forecast: Forecast
)