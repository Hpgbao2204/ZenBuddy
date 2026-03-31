package com.zenbuddy.domain.usecase.mood

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.MoodEntry
import com.zenbuddy.domain.repository.MoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMoodsUseCase @Inject constructor(
    private val moodRepository: MoodRepository
) {
    operator fun invoke(): Flow<Result<List<MoodEntry>>> =
        moodRepository.getMoods()
}
