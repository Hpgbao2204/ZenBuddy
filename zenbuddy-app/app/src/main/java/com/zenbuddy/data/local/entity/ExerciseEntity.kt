package com.zenbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zenbuddy.domain.model.Exercise
import java.util.UUID

@Entity(tableName = "exercises", indices = [Index("muscleGroup")])
data class ExerciseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val muscleGroup: String,     // "chest", "back", "legs", "arms", "shoulders", "core", "cardio"
    val difficulty: String,      // "beginner", "intermediate", "advanced"
    val durationMinutes: Int,
    val caloriesPerMinute: Double,
    val instructions: String,
    val imageUrl: String? = null,
    val isCustom: Boolean = false
)

fun ExerciseEntity.toDomain(): Exercise = Exercise(
    id = id,
    name = name,
    description = description,
    muscleGroup = muscleGroup,
    difficulty = difficulty,
    durationMinutes = durationMinutes,
    caloriesPerMinute = caloriesPerMinute,
    instructions = instructions,
    imageUrl = imageUrl,
    isCustom = isCustom
)

fun Exercise.toEntity(): ExerciseEntity = ExerciseEntity(
    id = id,
    name = name,
    description = description,
    muscleGroup = muscleGroup,
    difficulty = difficulty,
    durationMinutes = durationMinutes,
    caloriesPerMinute = caloriesPerMinute,
    instructions = instructions,
    imageUrl = imageUrl,
    isCustom = isCustom
)
