package com.zenbuddy.domain.usecase.insights

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.repository.ChatRepository
import com.zenbuddy.domain.repository.MoodRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetMoodInsightsUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val moodRepository: MoodRepository
) {
    suspend operator fun invoke(days: Int = 7): Result<String> {
        val moodScores = when (val result = moodRepository.getRecentMoodScores(days).first()) {
            is Result.Success -> result.data
            else -> emptyList()
        }
        return chatRepository.generateMoodInsight(moodScores, days)
    }
}
