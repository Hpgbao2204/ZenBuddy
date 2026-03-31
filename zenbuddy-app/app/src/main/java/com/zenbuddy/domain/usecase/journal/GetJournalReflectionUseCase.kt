package com.zenbuddy.domain.usecase.journal

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.repository.ChatRepository
import javax.inject.Inject

class GetJournalReflectionUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(journalText: String): Result<String> =
        chatRepository.generateJournalReflection(journalText)
}
