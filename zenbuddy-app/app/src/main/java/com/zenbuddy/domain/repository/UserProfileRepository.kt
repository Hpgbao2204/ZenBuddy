package com.zenbuddy.domain.repository

import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun getProfile(): Flow<Result<UserProfile>>
    suspend fun getProfileOnce(): Result<UserProfile>
    suspend fun saveProfile(profile: UserProfile): Result<Unit>
}
