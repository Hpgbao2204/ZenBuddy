package com.zenbuddy.domain.usecase.profile

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.UserProfile
import com.zenbuddy.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {
    operator fun invoke(): Flow<Result<UserProfile>> = userProfileRepository.getProfile()
}
