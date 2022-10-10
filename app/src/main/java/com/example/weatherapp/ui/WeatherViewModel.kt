package com.example.weatherapp.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weatherapp.repository.WeatherRepository
import com.example.weatherapp.response.WeatherResponse
import kotlinx.coroutines.launch
import retrofit2.Response

class WeatherViewModel(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    val weatherVM: MutableLiveData<com.example.weatherapp.util.Resource<WeatherResponse>> =
        MutableLiveData()

    fun getWeather(apiKey: String, city: String, days: Int, aqi: String, alerts: String) =
        viewModelScope.launch {
            weatherVM.postValue(com.example.weatherapp.util.Resource.Loading())
            val response = weatherRepository.getWeather(apiKey, city, days, aqi, alerts)
            weatherVM.postValue(handleWeatherResponse(response))
        }

    private fun handleWeatherResponse(response: Response<WeatherResponse>): com.example.weatherapp.util.Resource<WeatherResponse> {
        if (response.isSuccessful) {
            response.body()?.let { resultResponse ->
                return com.example.weatherapp.util.Resource.Success(resultResponse)
            }
        }
        return com.example.weatherapp.util.Resource.Error(response.message())
    }
}