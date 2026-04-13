package com.zenbuddy.domain.model

data class FoodEntry(
    val id: String = "",
    val name: String = "",
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val mealType: String = "snack",
    val imagePath: String? = null,
    val date: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
