package com.zenbuddy.domain.usecase.quest

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.repository.QuestRepository
import javax.inject.Inject

class CompleteQuestUseCase @Inject constructor(
    private val questRepository: QuestRepository
) {
    suspend operator fun invoke(questId: String): Result<Unit> =
        questRepository.completeQuest(questId)
}
