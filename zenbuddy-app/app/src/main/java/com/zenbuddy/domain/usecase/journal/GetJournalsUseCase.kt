package com.zenbuddy.domain.usecase.journal

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.JournalEntry
import com.zenbuddy.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetJournalsUseCase @Inject constructor(
    private val journalRepository: JournalRepository
) {
    operator fun invoke(): Flow<Result<List<JournalEntry>>> =
        journalRepository.getJournals()
}
