package com.zenbuddy.data.remote.dto

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val main: MainData,
    val weather: List<WeatherData>,
    val name: String
)

data class MainData(
    val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    val humidity: Int
)

data class WeatherData(
    val description: String,
    val icon: String
)
