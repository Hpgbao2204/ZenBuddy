package com.zenbuddy.domain.model

data class Exercise(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val muscleGroup: String = "",
    val difficulty: String = "beginner",
    val durationMinutes: Int = 0,
    val caloriesPerMinute: Double = 0.0,
    val instructions: String = "",
    val imageUrl: String? = null,
    val isCustom: Boolean = false
)
