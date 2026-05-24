package com.zenbuddy.data.repository

import com.zenbuddy.core.di.IoDispatcher
import com.zenbuddy.core.error.AppError
import com.zenbuddy.core.result.Result
import com.zenbuddy.data.remote.api.WeatherApiService
import com.zenbuddy.domain.model.WeatherInfo
import com.zenbuddy.domain.repository.WeatherRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val weatherApi: WeatherApiService,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : WeatherRepository {

    override suspend fun getWeather(lat: Double, lon: Double): Result<WeatherInfo> =
        withContext(ioDispatcher) {
            runCatching {
                val response = weatherApi.getWeather(lat, lon)
                val current = response.current
                val description = describeWeather(current.weatherCode)
                WeatherInfo(
                    temperature = current.temperature,
                    feelsLike = current.feelsLike,
                    humidity = current.humidity,
                    description = description,
                    icon = "",
                    city = "Vị trí hiện tại",
                    suggestion = generateSuggestion(current.temperature, description)
                )
            }.fold(
                onSuccess = { Result.Success(it) },
                onFailure = { Result.Error(AppError.NetworkError(it.message ?: "Weather fetch failed")) }
            )
        }

    private fun generateSuggestion(temp: Double, description: String): String {
        return when {
            temp > 35 -> "Thời tiết rất nóng! Nên tập trong nhà, uống nhiều nước."
            temp > 30 -> "Trời nóng, hãy tập sáng sớm hoặc chiều muộn để tránh nắng."
            temp in 25.0..30.0 -> "Thời tiết lý tưởng để chạy bộ hoặc đạp xe ngoài trời!"
            temp in 20.0..25.0 -> "Nhiệt độ dễ chịu, phù hợp cho mọi hoạt động thể thao."
            temp < 15 -> "Trời lạnh, hãy khởi động kỹ trước khi tập."
            description.contains("mưa", ignoreCase = true) -> "Trời mưa, hãy tập trong nhà hôm nay."
            else -> "Hãy vận động ít nhất 30 phút hôm nay!"
        }
    }

    private fun describeWeather(code: Int): String = when (code) {
        0 -> "trời quang"
        1, 2, 3 -> "có mây"
        45, 48 -> "sương mù"
        51, 53, 55, 56, 57 -> "mưa phùn"
        61, 63, 65, 66, 67 -> "mưa"
        71, 73, 75, 77 -> "tuyết"
        80, 81, 82 -> "mưa rào"
        85, 86 -> "mưa tuyết"
        95, 96, 99 -> "giông"
        else -> "thời tiết hiện tại"
    }
}
