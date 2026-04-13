package com.zenbuddy.domain.model

data class StepCount(
    val id: String = "",
    val date: String = "",
    val steps: Int = 0,
    val caloriesBurned: Double = 0.0,
    val distanceKm: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)
