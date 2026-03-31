package com.zenbuddy.domain.usecase.quest

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.Quest
import com.zenbuddy.domain.repository.ChatRepository
import com.zenbuddy.domain.repository.JournalRepository
import com.zenbuddy.domain.repository.MoodRepository
import com.zenbuddy.domain.repository.QuestRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class GenerateQuestsUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val moodRepository: MoodRepository,
    private val journalRepository: JournalRepository,
    private val questRepository: QuestRepository
) {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    suspend operator fun invoke(): Flow<Result<List<Quest>>> {
        val moodScore = when (val result = moodRepository.getTodayMood().first()) {
            is Result.Success -> result.data?.score ?: 5
            else -> 5
        }
        val journalSummary = when (val result = journalRepository.getRecentSummary()) {
            is Result.Success -> result.data
            else -> ""
        }

        val today = dateFormat.format(Date())

        return chatRepository.generateQuests(moodScore, journalSummary).map { result ->
            when (result) {
                is Result.Success -> {
                    val quests = result.data.map { title ->
                        Quest(title = title, generatedForDate = today)
                    }
                    questRepository.saveQuests(quests)
                    Result.Success(quests)
                }
                is Result.Error -> result
                is Result.Loading -> result
            }
        }
    }
}
