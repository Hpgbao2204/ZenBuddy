package com.zenbuddy.domain.repository

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.WeatherInfo

interface WeatherRepository {
    suspend fun getWeather(lat: Double, lon: Double): Result<WeatherInfo>
}
