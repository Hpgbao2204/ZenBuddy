package com.zenbuddy.domain.usecase.schedule

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.ScheduleEntry
import com.zenbuddy.domain.repository.ScheduleRepository
import javax.inject.Inject

class AddScheduleEntryUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(entry: ScheduleEntry): Result<Unit> =
        scheduleRepository.addEntry(entry)
}
