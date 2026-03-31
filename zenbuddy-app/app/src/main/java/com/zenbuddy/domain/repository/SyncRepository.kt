package com.zenbuddy.domain.repository

interface SyncRepository {
    suspend fun syncToCloud(): Result<Unit>
    suspend fun syncFromCloud(): Result<Unit>
}
