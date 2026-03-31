package com.zenbuddy.domain.usecase.journal

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.JournalEntry
import com.zenbuddy.domain.repository.JournalRepository
import javax.inject.Inject

class SaveJournalUseCase @Inject constructor(
    private val journalRepository: JournalRepository
) {
    suspend operator fun invoke(text: String, audioPath: String? = null): Result<Unit> {
        val entry = JournalEntry(text = text, audioPath = audioPath)
        return journalRepository.saveJournal(entry)
    }
}
