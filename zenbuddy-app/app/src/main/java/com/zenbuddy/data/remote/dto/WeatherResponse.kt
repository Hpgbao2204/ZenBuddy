package com.zenbuddy.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val current: CurrentWeatherData
)

data class CurrentWeatherData(
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("apparent_temperature") val feelsLike: Double,
    @SerializedName("relative_humidity_2m") val humidity: Int,
    @SerializedName("weather_code") val weatherCode: Int
)
