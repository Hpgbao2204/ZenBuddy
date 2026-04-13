package com.zenbuddy.data.repository

import com.zenbuddy.core.di.IoDispatcher
import com.zenbuddy.core.error.AppError
import com.zenbuddy.core.result.Result
import com.zenbuddy.data.local.dao.UserProfileDao
import com.zenbuddy.data.local.entity.toDomain
import com.zenbuddy.data.local.entity.toEntity
import com.zenbuddy.domain.model.UserProfile
import com.zenbuddy.domain.repository.UserProfileRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserProfileRepositoryImpl @Inject constructor(
    private val userProfileDao: UserProfileDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : UserProfileRepository {

    override fun getProfile(): Flow<Result<UserProfile>> =
        userProfileDao.getProfile()
            .map { entity -> Result.Success(entity?.toDomain() ?: UserProfile()) }
            .catch { emit(Result.Error(AppError.DatabaseError(it.message ?: "DB error"))) }
            .flowOn(ioDispatcher)

    override suspend fun getProfileOnce(): Result<UserProfile> =
        withContext(ioDispatcher) {
            runCatching { userProfileDao.getProfileOnce()?.toDomain() ?: UserProfile() }.fold(
                onSuccess = { Result.Success(it) },
                onFailure = { Result.Error(AppError.DatabaseError(it.message ?: "Query failed")) }
            )
        }

    override suspend fun saveProfile(profile: UserProfile): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching { userProfileDao.upsert(profile.toEntity()) }.fold(
                onSuccess = { Result.Success(Unit) },
                onFailure = { Result.Error(AppError.DatabaseError(it.message ?: "Save failed")) }
            )
        }
}
