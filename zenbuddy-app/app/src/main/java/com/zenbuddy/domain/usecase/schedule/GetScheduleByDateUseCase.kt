package com.zenbuddy.domain.usecase.schedule

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.ScheduleEntry
import com.zenbuddy.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetScheduleByDateUseCase @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) {
    operator fun invoke(date: String): Flow<Result<List<ScheduleEntry>>> =
        scheduleRepository.getByDate(date)
}
