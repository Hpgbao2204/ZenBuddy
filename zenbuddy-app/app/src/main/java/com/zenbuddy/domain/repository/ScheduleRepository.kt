package com.zenbuddy.domain.repository

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.ScheduleEntry
import kotlinx.coroutines.flow.Flow

interface ScheduleRepository {
    fun getByDate(date: String): Flow<Result<List<ScheduleEntry>>>
    fun getUpcoming(fromDate: String): Flow<Result<List<ScheduleEntry>>>
    suspend fun addEntry(entry: ScheduleEntry): Result<Unit>
    suspend fun updateEntry(entry: ScheduleEntry): Result<Unit>
    suspend fun markCompleted(id: String, completed: Boolean): Result<Unit>
    suspend fun deleteEntry(id: String): Result<Unit>
}
