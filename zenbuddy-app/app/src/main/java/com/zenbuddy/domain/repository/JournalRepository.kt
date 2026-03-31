package com.zenbuddy.domain.repository

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.JournalEntry
import kotlinx.coroutines.flow.Flow

interface JournalRepository {
    fun getJournals(): Flow<Result<List<JournalEntry>>>
    suspend fun saveJournal(entry: JournalEntry): Result<Unit>
    suspend fun getRecentSummary(limit: Int = 3): Result<String>
}
