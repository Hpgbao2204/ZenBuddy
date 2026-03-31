package com.zenbuddy.domain.repository

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.Quest
import kotlinx.coroutines.flow.Flow

interface QuestRepository {
    fun getQuestsForToday(): Flow<Result<List<Quest>>>
    fun getActiveQuestCount(): Flow<Result<Int>>
    suspend fun saveQuests(quests: List<Quest>): Result<Unit>
    suspend fun completeQuest(questId: String): Result<Unit>
}
