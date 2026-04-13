package com.zenbuddy.domain.usecase.profile

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.UserProfile
import com.zenbuddy.domain.repository.UserProfileRepository
import javax.inject.Inject

class SaveProfileUseCase @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) {
    suspend operator fun invoke(profile: UserProfile): Result<Unit> =
        userProfileRepository.saveProfile(profile)
}
