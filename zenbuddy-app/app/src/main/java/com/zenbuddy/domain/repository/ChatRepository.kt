package com.zenbuddy.domain.repository

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.ChatContext
import com.zenbuddy.domain.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun sendMessage(userMessage: String, context: ChatContext): Flow<Result<String>>
    fun generateQuests(moodScore: Int, recentJournal: String): Flow<Result<List<String>>>
    fun getMessages(sessionId: String): Flow<Result<List<ChatMessage>>>
    suspend fun saveMessage(message: ChatMessage): Result<Unit>
}
