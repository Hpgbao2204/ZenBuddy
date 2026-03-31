package com.zenbuddy.data.repository

import com.zenbuddy.core.di.IoDispatcher
import com.zenbuddy.core.error.AppError
import com.zenbuddy.core.result.Result
import com.zenbuddy.data.local.dao.JournalDao
import com.zenbuddy.data.local.entity.toDomain
import com.zenbuddy.data.local.entity.toEntity
import com.zenbuddy.domain.model.JournalEntry
import com.zenbuddy.domain.repository.AuthRepository
import com.zenbuddy.domain.repository.JournalRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class JournalRepositoryImpl @Inject constructor(
    private val journalDao: JournalDao,
    private val authRepository: AuthRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : JournalRepository {

    private fun uid(): String = authRepository.getCurrentUserId() ?: ""

    override fun getJournals(): Flow<Result<List<JournalEntry>>> =
        journalDao.getAllJournals(uid())
            .map<_, Result<List<JournalEntry>>> { entities ->
                Result.Success(entities.map { it.toDomain() })
            }
            .catch { emit(Result.Error(AppError.DatabaseError(it.message ?: "DB error"))) }
            .flowOn(ioDispatcher)

    override suspend fun saveJournal(entry: JournalEntry): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching { journalDao.insert(entry.toEntity(userId = uid())) }
                .fold(
                    onSuccess = { Result.Success(Unit) },
                    onFailure = { Result.Error(AppError.DatabaseError(it.message ?: "Insert failed")) }
                )
        }

    override suspend fun getRecentSummary(limit: Int): Result<String> =
        withContext(ioDispatcher) {
            runCatching {
                val entries = journalDao.getRecent(uid(), limit)
                entries.joinToString("; ") { it.text.take(100) }
            }.fold(
                onSuccess = { Result.Success(it) },
                onFailure = { Result.Error(AppError.DatabaseError(it.message ?: "Query failed")) }
            )
        }
}
