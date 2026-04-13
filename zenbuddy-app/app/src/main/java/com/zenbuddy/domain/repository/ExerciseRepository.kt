package com.zenbuddy.domain.repository

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.Exercise
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun getAllExercises(): Flow<Result<List<Exercise>>>
    fun getByMuscleGroup(group: String): Flow<Result<List<Exercise>>>
    fun getMuscleGroups(): Flow<Result<List<String>>>
    suspend fun getById(id: String): Result<Exercise>
    suspend fun seedIfEmpty()
}
