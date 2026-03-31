package com.zenbuddy.data.repository

import com.zenbuddy.core.di.IoDispatcher
import com.zenbuddy.core.error.AppError
import com.zenbuddy.core.result.Result
import com.zenbuddy.data.local.dao.QuestDao
import com.zenbuddy.data.local.entity.toDomain
import com.zenbuddy.data.local.entity.toEntity
import com.zenbuddy.domain.model.Quest
import com.zenbuddy.domain.repository.QuestRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class QuestRepositoryImpl @Inject constructor(
    private val questDao: QuestDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : QuestRepository {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun getQuestsForToday(): Flow<Result<List<Quest>>> {
        val today = dateFormat.format(Date())
        return questDao.getQuestsForDate(today)
            .map<_, Result<List<Quest>>> { entities ->
                Result.Success(entities.map { it.toDomain() })
            }
            .catch { emit(Result.Error(AppError.DatabaseError(it.message ?: "DB error"))) }
            .flowOn(ioDispatcher)
    }

    override fun getActiveQuestCount(): Flow<Result<Int>> {
        val today = dateFormat.format(Date())
        return questDao.getActiveQuestCount(today)
            .map<_, Result<Int>> { count -> Result.Success(count) }
            .catch { emit(Result.Error(AppError.DatabaseError(it.message ?: "DB error"))) }
            .flowOn(ioDispatcher)
    }

    override suspend fun saveQuests(quests: List<Quest>): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching { questDao.insertAll(quests.map { it.toEntity() }) }
                .fold(
                    onSuccess = { Result.Success(Unit) },
                    onFailure = { Result.Error(AppError.DatabaseError(it.message ?: "Insert failed")) }
                )
        }

    override suspend fun completeQuest(questId: String): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching {
                val quest = questDao.getById(questId)
                    ?: throw IllegalStateException("Quest not found")
                questDao.update(quest.copy(isCompleted = true))
            }.fold(
                onSuccess = { Result.Success(Unit) },
                onFailure = { Result.Error(AppError.DatabaseError(it.message ?: "Update failed")) }
            )
        }
}
