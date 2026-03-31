package com.zenbuddy.domain.usecase.chat

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.ChatContext
import com.zenbuddy.domain.model.ChatMessage
import com.zenbuddy.domain.repository.ChatRepository
import com.zenbuddy.domain.repository.JournalRepository
import com.zenbuddy.domain.repository.MoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val moodRepository: MoodRepository,
    private val journalRepository: JournalRepository
) {
    suspend operator fun invoke(
        userMessage: String,
        sessionId: String
    ): Flow<Result<String>> {
        val userMsg = ChatMessage(
            text = userMessage,
            isFromUser = true,
            sessionId = sessionId
        )
        chatRepository.saveMessage(userMsg)

        val moodScores = when (val result = moodRepository.getRecentMoodScores().first()) {
            is Result.Success -> result.data
            else -> emptyList()
        }
        val journalSummary = when (val result = journalRepository.getRecentSummary()) {
            is Result.Success -> result.data
            else -> ""
        }

        val context = ChatContext(
            moodHistory = moodScores,
            journalSummary = journalSummary
        )

        return chatRepository.sendMessage(userMessage, context)
    }
}
