package com.zenbuddy.data.remote.dto

import com.zenbuddy.data.local.entity.JournalEntity
import kotlinx.serialization.Serializable

@Serializable
data class JournalDto(
    val id: String,
    val text: String,
    val audio_path: String? = null,
    val created_at: Long
)

fun JournalEntity.toDto(): JournalDto = JournalDto(
    id = id,
    text = text,
    audio_path = audioPath,
    created_at = createdAt
)

fun JournalDto.toEntity(): JournalEntity = JournalEntity(
    id = id,
    text = text,
    audioPath = audio_path,
    isSynced = true,
    createdAt = created_at
)
