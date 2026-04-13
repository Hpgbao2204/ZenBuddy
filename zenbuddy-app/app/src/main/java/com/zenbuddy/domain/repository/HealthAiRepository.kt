package com.zenbuddy.domain.repository

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.ScheduleEntry
import kotlinx.coroutines.flow.Flow

interface HealthAiRepository {
    fun generateMealPlan(
        tdee: Double,
        goalType: String,
        preferences: String
    ): Flow<Result<String>>

    fun generateWorkoutPlan(
        profile: String,
        goalType: String
    ): Flow<Result<String>>

    fun generateDailySchedule(
        profileSummary: String,
        currentSchedule: List<ScheduleEntry>
    ): Flow<Result<List<ScheduleEntry>>>

    fun getHealthAdvice(
        question: String,
        profileSummary: String,
        todaySteps: Int,
        todayCalories: Double
    ): Flow<Result<String>>
}
