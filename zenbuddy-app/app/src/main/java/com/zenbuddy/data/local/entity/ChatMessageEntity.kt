package com.zenbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zenbuddy.domain.model.ChatMessage
import java.util.UUID

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val text: String,
    val isFromUser: Boolean,
    val sessionId: String,
    val createdAt: Long = System.currentTimeMillis()
)

fun ChatMessageEntity.toDomain(): ChatMessage = ChatMessage(
    id = id,
    text = text,
    isFromUser = isFromUser,
    sessionId = sessionId,
    createdAt = createdAt
)

fun ChatMessage.toEntity(userId: String = ""): ChatMessageEntity = ChatMessageEntity(
    id = id,
    userId = userId,
    text = text,
    isFromUser = isFromUser,
    sessionId = sessionId,
    createdAt = createdAt
)
