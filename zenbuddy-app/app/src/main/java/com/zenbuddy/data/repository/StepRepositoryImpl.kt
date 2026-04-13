package com.zenbuddy.data.repository

import com.zenbuddy.core.di.IoDispatcher
import com.zenbuddy.core.error.AppError
import com.zenbuddy.core.result.Result
import com.zenbuddy.data.local.dao.StepDao
import com.zenbuddy.data.local.entity.StepCountEntity
import com.zenbuddy.data.local.entity.toDomain
import com.zenbuddy.domain.model.StepCount
import com.zenbuddy.domain.repository.StepRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

class StepRepositoryImpl @Inject constructor(
    private val stepDao: StepDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : StepRepository {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun getTodaySteps(): Flow<Result<StepCount>> {
        val today = LocalDate.now().format(dateFormatter)
        return stepDao.getStepsByDate(today)
            .map<_, Result<StepCount>> { entity ->
                Result.Success(entity?.toDomain() ?: StepCount(date = today))
            }
            .catch { emit(Result.Error(AppError.DatabaseError(it.message ?: "DB error"))) }
            .flowOn(ioDispatcher)
    }

    override fun getWeeklySteps(): Flow<Result<List<StepCount>>> {
        val weekAgo = LocalDate.now().minusDays(6).format(dateFormatter)
        return stepDao.getStepsFrom(weekAgo)
            .map<_, Result<List<StepCount>>> { entities -> Result.Success(entities.map { it.toDomain() }) }
            .catch { emit(Result.Error(AppError.DatabaseError(it.message ?: "DB error"))) }
            .flowOn(ioDispatcher)
    }

    override suspend fun updateSteps(date: String, steps: Int, weightKg: Double): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching {
                val calories = steps * 0.04 * (weightKg / 70.0)
                val distance = steps * 0.000762
                val existing = stepDao.getStepsByDate(date)
                stepDao.upsert(
                    StepCountEntity(
                        id = UUID.randomUUID().toString(),
                        date = date,
                        steps = steps,
                        caloriesBurned = calories,
                        distanceKm = distance
                    )
                )
            }.fold(
                onSuccess = { Result.Success(Unit) },
                onFailure = { Result.Error(AppError.DatabaseError(it.message ?: "Update failed")) }
            )
        }
}
