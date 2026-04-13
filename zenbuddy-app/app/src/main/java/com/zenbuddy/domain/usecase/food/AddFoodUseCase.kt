package com.zenbuddy.domain.usecase.food

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.FoodEntry
import com.zenbuddy.domain.repository.FoodRepository
import javax.inject.Inject

class AddFoodUseCase @Inject constructor(
    private val foodRepository: FoodRepository
) {
    suspend operator fun invoke(entry: FoodEntry): Result<Unit> = foodRepository.addFood(entry)
}
