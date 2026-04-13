package com.zenbuddy.data.repository

import com.zenbuddy.core.di.IoDispatcher
import com.zenbuddy.core.error.AppError
import com.zenbuddy.core.result.Result
import com.zenbuddy.data.local.dao.ScheduleDao
import com.zenbuddy.data.local.entity.toDomain
import com.zenbuddy.data.local.entity.toEntity
import com.zenbuddy.domain.model.ScheduleEntry
import com.zenbuddy.domain.repository.ScheduleRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ScheduleRepositoryImpl @Inject constructor(
    private val scheduleDao: ScheduleDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ScheduleRepository {

    override fun getByDate(date: String): Flow<Result<List<ScheduleEntry>>> =
        scheduleDao.getByDate(date)
            .map<_, Result<List<ScheduleEntry>>> { entities -> Result.Success(entities.map { it.toDomain() }) }
            .catch { emit(Result.Error(AppError.DatabaseError(it.message ?: "DB error"))) }
            .flowOn(ioDispatcher)

    override fun getUpcoming(fromDate: String): Flow<Result<List<ScheduleEntry>>> =
        scheduleDao.getUpcoming(fromDate)
            .map<_, Result<List<ScheduleEntry>>> { entities -> Result.Success(entities.map { it.toDomain() }) }
            .catch { emit(Result.Error(AppError.DatabaseError(it.message ?: "DB error"))) }
            .flowOn(ioDispatcher)

    override suspend fun addEntry(entry: ScheduleEntry): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching { scheduleDao.insert(entry.toEntity()) }.fold(
                onSuccess = { Result.Success(Unit) },
                onFailure = { Result.Error(AppError.DatabaseError(it.message ?: "Insert failed")) }
            )
        }

    override suspend fun updateEntry(entry: ScheduleEntry): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching { scheduleDao.update(entry.toEntity()) }.fold(
                onSuccess = { Result.Success(Unit) },
                onFailure = { Result.Error(AppError.DatabaseError(it.message ?: "Update failed")) }
            )
        }

    override suspend fun markCompleted(id: String, completed: Boolean): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching { scheduleDao.markCompleted(id, completed) }.fold(
                onSuccess = { Result.Success(Unit) },
                onFailure = { Result.Error(AppError.DatabaseError(it.message ?: "Update failed")) }
            )
        }

    override suspend fun deleteEntry(id: String): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching { scheduleDao.deleteById(id) }.fold(
                onSuccess = { Result.Success(Unit) },
                onFailure = { Result.Error(AppError.DatabaseError(it.message ?: "Delete failed")) }
            )
        }
}
