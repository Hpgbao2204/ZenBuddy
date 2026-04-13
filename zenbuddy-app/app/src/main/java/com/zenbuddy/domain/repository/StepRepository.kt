package com.zenbuddy.domain.repository

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.StepCount
import kotlinx.coroutines.flow.Flow

interface StepRepository {
    fun getTodaySteps(): Flow<Result<StepCount>>
    fun getWeeklySteps(): Flow<Result<List<StepCount>>>
    suspend fun updateSteps(date: String, steps: Int, weightKg: Double): Result<Unit>
}
