package com.zenbuddy.domain.model

data class WeatherInfo(
    val temperature: Double = 0.0,
    val feelsLike: Double = 0.0,
    val humidity: Int = 0,
    val description: String = "",
    val icon: String = "",
    val city: String = "",
    val suggestion: String = ""
)
