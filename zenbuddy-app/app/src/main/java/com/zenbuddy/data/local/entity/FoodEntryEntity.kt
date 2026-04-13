package com.zenbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zenbuddy.domain.model.FoodEntry
import java.util.UUID

@Entity(tableName = "food_entries", indices = [Index("date")])
data class FoodEntryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val calories: Double,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0,
    val mealType: String,        // "breakfast", "lunch", "dinner", "snack"
    val imagePath: String? = null,
    val date: String,            // "2026-04-13"
    val createdAt: Long = System.currentTimeMillis()
)

fun FoodEntryEntity.toDomain(): FoodEntry = FoodEntry(
    id = id,
    name = name,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    mealType = mealType,
    imagePath = imagePath,
    date = date,
    createdAt = createdAt
)

fun FoodEntry.toEntity(): FoodEntryEntity = FoodEntryEntity(
    id = id,
    name = name,
    calories = calories,
    protein = protein,
    carbs = carbs,
    fat = fat,
    mealType = mealType,
    imagePath = imagePath,
    date = date,
    createdAt = createdAt
)
