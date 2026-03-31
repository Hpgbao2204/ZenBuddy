package com.zenbuddy.domain.usecase.insights

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.repository.ChatRepository
import com.zenbuddy.domain.repository.JournalRepository
import com.zenbuddy.domain.repository.MoodRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetDailyAffirmationUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val moodRepository: MoodRepository,
    private val journalRepository: JournalRepository
) {
    suspend operator fun invoke(): Result<String> {
        val moodScore = when (val result = moodRepository.getTodayMood().first()) {
            is Result.Success -> result.data?.score ?: 5
            else -> 5
        }
        val journalSummary = when (val result = journalRepository.getRecentSummary()) {
            is Result.Success -> result.data
            else -> ""
        }
        return chatRepository.generateAffirmation(moodScore, journalSummary)
    }
}
