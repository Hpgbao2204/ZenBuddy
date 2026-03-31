package com.zenbuddy.data.remote.dto

import com.zenbuddy.data.local.entity.ChatMessageEntity
import kotlinx.serialization.Serializable

@Serializable
data class ChatMessageDto(
    val id: String,
    val text: String,
    val is_from_user: Boolean,
    val session_id: String,
    val created_at: Long
)

fun ChatMessageEntity.toDto(): ChatMessageDto = ChatMessageDto(
    id = id,
    text = text,
    is_from_user = isFromUser,
    session_id = sessionId,
    created_at = createdAt
)

fun ChatMessageDto.toEntity(): ChatMessageEntity = ChatMessageEntity(
    id = id,
    text = text,
    isFromUser = is_from_user,
    sessionId = session_id,
    createdAt = created_at
)
