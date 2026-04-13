package com.zenbuddy.domain.usecase.food

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.FoodEntry
import com.zenbuddy.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AnalyzeFoodImageUseCase @Inject constructor(
    private val foodRepository: FoodRepository
) {
    operator fun invoke(imageBytes: ByteArray): Flow<Result<FoodEntry>> =
        foodRepository.analyzeFoodImage(imageBytes)
}
