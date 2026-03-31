package com.zenbuddy.data.repository

import com.zenbuddy.core.di.IoDispatcher
import com.zenbuddy.core.error.AppError
import com.zenbuddy.core.result.Result
import com.zenbuddy.data.local.dao.MoodDao
import com.zenbuddy.data.local.entity.toDomain
import com.zenbuddy.data.local.entity.toEntity
import com.zenbuddy.domain.model.MoodEntry
import com.zenbuddy.domain.repository.AuthRepository
import com.zenbuddy.domain.repository.MoodRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar
import javax.inject.Inject

class MoodRepositoryImpl @Inject constructor(
    private val moodDao: MoodDao,
    private val authRepository: AuthRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : MoodRepository {

    private fun uid(): String = authRepository.getCurrentUserId() ?: ""

    override fun getMoods(): Flow<Result<List<MoodEntry>>> =
        moodDao.getAllMoods(uid())
            .map<_, Result<List<MoodEntry>>> { entities ->
                Result.Success(entities.map { it.toDomain() })
            }
            .catch { emit(Result.Error(AppError.DatabaseError(it.message ?: "DB error"))) }
            .flowOn(ioDispatcher)

    override fun getTodayMood(): Flow<Result<MoodEntry?>> {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        return moodDao.getTodayMood(uid(), startOfDay)
            .map<_, Result<MoodEntry?>> { entity ->
                Result.Success(entity?.toDomain())
            }
            .catch { emit(Result.Error(AppError.DatabaseError(it.message ?: "DB error"))) }
            .flowOn(ioDispatcher)
    }

    override fun getRecentMoodScores(days: Int): Flow<Result<List<Int>>> {
        val from = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return moodDao.getRecentMoods(uid(), from, days)
            .map<_, Result<List<Int>>> { entities ->
                Result.Success(entities.map { it.score })
            }
            .catch { emit(Result.Error(AppError.DatabaseError(it.message ?: "DB error"))) }
            .flowOn(ioDispatcher)
    }

    override fun getMoodsForPeriod(days: Int): Flow<Result<List<MoodEntry>>> {
        val from = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return moodDao.getMoodsFrom(uid(), from)
            .map<_, Result<List<MoodEntry>>> { entities ->
                Result.Success(entities.map { it.toDomain() })
            }
            .catch { emit(Result.Error(AppError.DatabaseError(it.message ?: "DB error"))) }
            .flowOn(ioDispatcher)
    }

    override suspend fun logMood(entry: MoodEntry): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching { moodDao.insert(entry.toEntity(userId = uid())) }
                .fold(
                    onSuccess = { Result.Success(Unit) },
                    onFailure = { Result.Error(AppError.DatabaseError(it.message ?: "Insert failed")) }
                )
        }
}
