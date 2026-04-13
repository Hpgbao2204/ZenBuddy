package com.zenbuddy.ui.feature.profile

import com.zenbuddy.domain.model.UserProfile

data class ProfileUiState(
    val profile: UserProfile = UserProfile(),
    val tdee: Double = 0.0,
    val bmi: Double = 0.0,
    val bmr: Double = 0.0,
    val isSaving: Boolean = false,
    val isLoading: Boolean = true,
    val saveSuccess: Boolean = false,
    val error: String? = null
)

sealed interface ProfileUiEvent {
    data class UpdateName(val name: String) : ProfileUiEvent
    data class UpdateAge(val age: Int) : ProfileUiEvent
    data class UpdateGender(val gender: String) : ProfileUiEvent
    data class UpdateHeight(val height: Double) : ProfileUiEvent
    data class UpdateWeight(val weight: Double) : ProfileUiEvent
    data class UpdateActivityLevel(val level: String) : ProfileUiEvent
    data class UpdateGoalType(val goal: String) : ProfileUiEvent
    data class UpdateStepGoal(val goal: Int) : ProfileUiEvent
    data object SaveProfile : ProfileUiEvent
}
