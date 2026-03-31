package com.zenbuddy.domain.repository

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.MoodEntry
import kotlinx.coroutines.flow.Flow

interface MoodRepository {
    fun getMoods(): Flow<Result<List<MoodEntry>>>
    fun getTodayMood(): Flow<Result<MoodEntry?>>
    fun getRecentMoodScores(days: Int = 7): Flow<Result<List<Int>>>
    suspend fun logMood(entry: MoodEntry): Result<Unit>
}
