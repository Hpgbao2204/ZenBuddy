package com.zenbuddy.domain.repository

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.FoodEntry
import kotlinx.coroutines.flow.Flow

interface FoodRepository {
    fun getFoodByDate(date: String): Flow<Result<List<FoodEntry>>>
    fun getTotalCaloriesByDate(date: String): Flow<Result<Double>>
    suspend fun addFood(entry: FoodEntry): Result<Unit>
    suspend fun deleteFood(id: String): Result<Unit>
    fun analyzeFoodImage(imageBytes: ByteArray): Flow<Result<FoodEntry>>
}
