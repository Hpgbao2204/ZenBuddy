package com.zenbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zenbuddy.domain.model.StepCount
import java.util.UUID

@Entity(tableName = "step_counts", indices = [Index("date", unique = true)])
data class StepCountEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val date: String,            // "2026-04-13"
    val steps: Int = 0,
    val caloriesBurned: Double = 0.0,
    val distanceKm: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis()
)

fun StepCountEntity.toDomain(): StepCount = StepCount(
    id = id,
    date = date,
    steps = steps,
    caloriesBurned = caloriesBurned,
    distanceKm = distanceKm,
    createdAt = createdAt
)

fun StepCount.toEntity(): StepCountEntity = StepCountEntity(
    id = id,
    date = date,
    steps = steps,
    caloriesBurned = caloriesBurned,
    distanceKm = distanceKm,
    createdAt = createdAt
)
