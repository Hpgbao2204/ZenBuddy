package com.zenbuddy.data.repository

import com.zenbuddy.domain.repository.SyncRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor() : SyncRepository {
    override suspend fun syncToCloud(): Result<Unit> = Result.success(Unit)

    override suspend fun syncFromCloud(): Result<Unit> = Result.success(Unit)
}
