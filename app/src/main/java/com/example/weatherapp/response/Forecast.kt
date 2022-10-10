package com.example.weatherapp.response

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "weather"
)
data class Forecast(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    val forecastday: List<Forecastday>
)