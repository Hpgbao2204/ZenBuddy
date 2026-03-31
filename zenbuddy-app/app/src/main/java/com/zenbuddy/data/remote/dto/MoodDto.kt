package com.zenbuddy.data.remote.dto

import com.zenbuddy.data.local.entity.MoodEntity
import kotlinx.serialization.Serializable

@Serializable
data class MoodDto(
    val id: String,
    val score: Int,
    val note: String? = null,
    val created_at: Long
)

fun MoodEntity.toDto(): MoodDto = MoodDto(
    id = id,
    score = score,
    note = note,
    created_at = createdAt
)

fun MoodDto.toEntity(): MoodEntity = MoodEntity(
    id = id,
    score = score,
    note = note,
    isSynced = true,
    createdAt = created_at
)
