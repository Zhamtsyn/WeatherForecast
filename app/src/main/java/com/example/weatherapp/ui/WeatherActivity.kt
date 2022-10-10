package com.example.weatherapp.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bumptech.glide.Glide
import com.example.weatherapp.R
import com.example.weatherapp.adapters.WeatherAdapter
import com.example.weatherapp.repository.WeatherRepository
import com.example.weatherapp.response.Hour
import com.example.weatherapp.util.Constants
import com.example.weatherapp.util.Resource
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.android.synthetic.main.activity_weather.*

class WeatherActivity : AppCompatActivity() {

    lateinit var viewModel: WeatherViewModel

    private lateinit var weatherAdapter: WeatherAdapter
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private lateinit var fLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        val weatherRepository = WeatherRepository()
        val viewModelProviderFactory = WeatherViewModelProviderFactory(weatherRepository)
        viewModel =
            ViewModelProvider(this, viewModelProviderFactory)[WeatherViewModel::class.java]

        (rvWeather?.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        checkPermission()
        init()
        getLocation()
        initRcView()
    }

    private fun getWeatherData() {
        val hourList = ArrayList<Hour>()
        viewModel.weatherVM.observe(this, { response ->
            when (response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data.let { weatherResponse ->
                        if (weatherResponse != null) {
                            val fromHour =
                                weatherResponse.location.localtime.split(" ", ":")[1].toInt()

                            for (i in fromHour until weatherResponse.forecast.forecastday[0].hour.size) {
                                hourList.add(weatherResponse.forecast.forecastday[0].hour[i])
                            }

                            weatherAdapter.differ.submitList(hourList)

                            with(weatherResponse) {
                                val signCurrent =
                                    if (current.temp_c.toString().startsWith("-")) "-" else "+"
                                val signMax = if (forecast.forecastday[0].day.maxtemp_c.toString()
                                        .startsWith("-")
                                ) "-" else "+"
                                val signMin = if (forecast.forecastday[0].day.mintemp_c.toString()
                                        .startsWith("-")
                                ) "-" else "+"
                                val temp = "$signCurrent${current.temp_c.toString().split(".")[0]}°"
                                val maxMinTemp = "макс: $signMax${
                                    forecast.forecastday[0].day.maxtemp_c.toString().toFloat()
                                        .toInt()
                                }° | " +
                                        "мін: $signMin${
                                            forecast.forecastday[0].day.mintemp_c.toString()
                                                .toFloat().toInt()
                                        }°"
                                val date = setDate(location.localtime)

                                tvCity.text = location.name
                                tvTemperatureMain.text = temp
                                tvWeatherNameMain.text = current.condition.text
                                tvMaxMinTemp.text = maxMinTemp
                                tvDay.text = date
                                Glide.with(applicationContext)
                                    .load("https:" + current.condition.icon).into(ivWeatherMain)
                            }
                            with(weatherResponse.forecast.forecastday[0].hour[fromHour]) {
                                val feelsLike = "Відчувається як: ${feelslike_c.toFloat().toInt()}°"
                                val humidity = "Вологість: ${humidity}%"
                                val wind = "Швидкість вітру: ${wind_mph.toFloat().toInt()} м/с"
                                val hour = "${time.split(" ", ":")[1]}:00 - ${condition.text}"

                                tvChosenHour.text = hour
                                tvFeelsLike.text = feelsLike
                                tvHumidity.text = humidity
                                tvWind.text = wind
                            }

                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let {
                        Log.e("TAG", "An error occurred: $it")
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })
    }

    private fun init() {
        fLocationClient = LocationServices.getFusedLocationProviderClient(this@WeatherActivity)
    }

    private fun hideProgressBar() {
        tvCity.visibility = View.VISIBLE
        cv.visibility = View.VISIBLE
        pbLoading.visibility = View.INVISIBLE
    }

    private fun showProgressBar() {
        pbLoading.visibility = View.VISIBLE
        tvCity.visibility = View.INVISIBLE
        cv.visibility = View.INVISIBLE
    }

    private fun permissionListener() {
        pLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            getLocation()
        }
    }

    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun getLocation() {
        val ct = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fLocationClient.getCurrentLocation(LocationRequest.PRIORITY_HIGH_ACCURACY, ct.token)
            .addOnCompleteListener {
                val city = "${it.result.latitude},${it.result.longitude}"
                tvCity.text = city
                viewModel.getWeather(Constants.API_KEY, city, 1, "no", "no")
                getWeatherData()
            }
    }

    private fun initRcView() {
        rvWeather.layoutManager =
            LinearLayoutManager(this@WeatherActivity, RecyclerView.HORIZONTAL, false)
        weatherAdapter = WeatherAdapter { setHourInfo(it) }
        rvWeather.adapter = weatherAdapter
    }

    private fun setHourInfo(item: Hour) {
        val feelsLike = "Відчувається як: ${item.feelslike_c.toFloat().toInt()}°"
        val humidity = "Вологість: ${item.humidity}%"
        val wind = "Швидкість вітру: ${item.wind_kph.toFloat().toInt()} м/с"
        val hour = "${item.time.split(" ", ":")[1]}:00 - ${item.condition.text}"

        tvChosenHour.text = hour
        tvFeelsLike.text = feelsLike
        tvHumidity.text = humidity
        tvWind.text = wind
    }

    private fun setDate(time: String): String {
        var day = time.split("-", " ")[2]
        if (day.startsWith("0")) day = day.drop(1)
        var month = time.split("-")[1]
        when (month) {
            "01" -> month = "січня"
            "02" -> month = "лютого"
            "03" -> month = "березня"
            "04" -> month = "квітня"
            "05" -> month = "травня"
            "06" -> month = "червня"
            "07" -> month = "липня"
            "08" -> month = "серпня"
            "09" -> month = "вересня"
            "10" -> month = "жовтня"
            "11" -> month = "листопада"
            "12" -> month = "грудня"
        }
        return "$day $month"
    }
}