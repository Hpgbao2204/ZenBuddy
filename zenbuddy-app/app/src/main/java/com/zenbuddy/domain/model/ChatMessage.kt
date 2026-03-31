package com.zenbuddy.domain.model

import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isFromUser: Boolean,
    val sessionId: String,
    val createdAt: Long = System.currentTimeMillis()
)
