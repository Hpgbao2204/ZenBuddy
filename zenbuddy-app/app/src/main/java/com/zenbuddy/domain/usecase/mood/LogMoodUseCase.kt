package com.zenbuddy.domain.usecase.mood

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.MoodEntry
import com.zenbuddy.domain.repository.MoodRepository
import javax.inject.Inject

class LogMoodUseCase @Inject constructor(
    private val moodRepository: MoodRepository
) {
    suspend operator fun invoke(score: Int, note: String?): Result<Unit> {
        val entry = MoodEntry(score = score, note = note)
        return moodRepository.logMood(entry)
    }
}
