package com.zenbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zenbuddy.domain.model.UserProfile

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val id: String = "default",
    val name: String = "",
    val age: Int = 25,
    val gender: String = "male",         // "male", "female", "other"
    val heightCm: Double = 170.0,
    val weightKg: Double = 70.0,
    val activityLevel: String = "moderate", // "sedentary", "light", "moderate", "active", "very_active"
    val goalType: String = "maintain",   // "lose", "maintain", "gain"
    val dailyStepGoal: Int = 10000,
    val dailyCalorieGoal: Double = 2000.0,
    val updatedAt: Long = System.currentTimeMillis()
)

fun UserProfileEntity.toDomain(): UserProfile = UserProfile(
    id = id,
    name = name,
    age = age,
    gender = gender,
    heightCm = heightCm,
    weightKg = weightKg,
    activityLevel = activityLevel,
    goalType = goalType,
    dailyStepGoal = dailyStepGoal,
    dailyCalorieGoal = dailyCalorieGoal,
    updatedAt = updatedAt
)

fun UserProfile.toEntity(): UserProfileEntity = UserProfileEntity(
    id = id,
    name = name,
    age = age,
    gender = gender,
    heightCm = heightCm,
    weightKg = weightKg,
    activityLevel = activityLevel,
    goalType = goalType,
    dailyStepGoal = dailyStepGoal,
    dailyCalorieGoal = dailyCalorieGoal,
    updatedAt = updatedAt
)
