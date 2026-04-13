package com.zenbuddy.domain.model

data class UserProfile(
    val id: String = "default",
    val name: String = "",
    val age: Int = 25,
    val gender: String = "male",
    val heightCm: Double = 170.0,
    val weightKg: Double = 70.0,
    val activityLevel: String = "moderate",
    val goalType: String = "maintain",
    val dailyStepGoal: Int = 10000,
    val dailyCalorieGoal: Double = 2000.0,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun calculateBMR(): Double {
        return if (gender == "male") {
            88.362 + (13.397 * weightKg) + (4.799 * heightCm) - (5.677 * age)
        } else {
            447.593 + (9.247 * weightKg) + (3.098 * heightCm) - (4.330 * age)
        }
    }

    fun calculateTDEE(): Double {
        val bmr = calculateBMR()
        val multiplier = when (activityLevel) {
            "sedentary" -> 1.2
            "light" -> 1.375
            "moderate" -> 1.55
            "active" -> 1.725
            "very_active" -> 1.9
            else -> 1.55
        }
        return bmr * multiplier
    }

    fun calculateBMI(): Double {
        val heightM = heightCm / 100.0
        return weightKg / (heightM * heightM)
    }
}
