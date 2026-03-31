package com.zenbuddy.data.remote.dto

import com.zenbuddy.data.local.entity.QuestEntity
import kotlinx.serialization.Serializable

@Serializable
data class QuestDto(
    val id: String,
    val title: String,
    val is_completed: Boolean,
    val generated_for_date: String,
    val created_at: Long
)

fun QuestEntity.toDto(): QuestDto = QuestDto(
    id = id,
    title = title,
    is_completed = isCompleted,
    generated_for_date = generatedForDate,
    created_at = createdAt
)

fun QuestDto.toEntity(): QuestEntity = QuestEntity(
    id = id,
    title = title,
    isCompleted = is_completed,
    generatedForDate = generated_for_date,
    isSynced = true,
    createdAt = created_at
)
