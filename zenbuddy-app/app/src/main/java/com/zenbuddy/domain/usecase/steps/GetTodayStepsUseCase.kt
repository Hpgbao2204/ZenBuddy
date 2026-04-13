package com.zenbuddy.domain.usecase.steps

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.StepCount
import com.zenbuddy.domain.repository.StepRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTodayStepsUseCase @Inject constructor(
    private val stepRepository: StepRepository
) {
    operator fun invoke(): Flow<Result<StepCount>> = stepRepository.getTodaySteps()
}
