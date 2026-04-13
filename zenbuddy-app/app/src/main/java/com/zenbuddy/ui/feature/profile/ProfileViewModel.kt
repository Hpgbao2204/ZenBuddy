package com.zenbuddy.ui.feature.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun onEvent(event: ProfileUiEvent) {
        when (event) {
            is ProfileUiEvent.UpdateName -> updateProfile { it.copy(name = event.name) }
            is ProfileUiEvent.UpdateAge -> updateProfile { it.copy(age = event.age) }
            is ProfileUiEvent.UpdateGender -> updateProfile { it.copy(gender = event.gender) }
            is ProfileUiEvent.UpdateHeight -> updateProfile { it.copy(heightCm = event.height) }
            is ProfileUiEvent.UpdateWeight -> updateProfile { it.copy(weightKg = event.weight) }
            is ProfileUiEvent.UpdateActivityLevel -> updateProfile { it.copy(activityLevel = event.level) }
            is ProfileUiEvent.UpdateGoalType -> updateProfile { it.copy(goalType = event.goal) }
            is ProfileUiEvent.UpdateStepGoal -> updateProfile { it.copy(dailyStepGoal = event.goal) }
            ProfileUiEvent.SaveProfile -> saveProfile()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            userProfileRepository.getProfile().collect { result ->
                when (result) {
                    is Result.Success -> {
                        val profile = result.data
                        _uiState.update {
                            it.copy(
                                profile = profile,
                                tdee = profile.calculateTDEE(),
                                bmi = profile.calculateBMI(),
                                bmr = profile.calculateBMR(),
                                isLoading = false
                            )
                        }
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(error = result.error.message, isLoading = false)
                    }
                    Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private fun updateProfile(transform: (com.zenbuddy.domain.model.UserProfile) -> com.zenbuddy.domain.model.UserProfile) {
        val newProfile = transform(_uiState.value.profile)
        _uiState.update {
            it.copy(
                profile = newProfile,
                tdee = newProfile.calculateTDEE(),
                bmi = newProfile.calculateBMI(),
                bmr = newProfile.calculateBMR(),
                dailyCalorieGoal = newProfile.calculateTDEE()
            )
        }
    }

    private fun saveProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val profile = _uiState.value.profile.copy(
                dailyCalorieGoal = _uiState.value.tdee,
                updatedAt = System.currentTimeMillis()
            )
            val result = userProfileRepository.saveProfile(profile)
            _uiState.update {
                it.copy(
                    isSaving = false,
                    saveSuccess = result is Result.Success,
                    error = (result as? Result.Error)?.error?.message
                )
            }
        }
    }
}

private val ProfileUiState.dailyCalorieGoal: Double
    get() = profile.dailyCalorieGoal

private fun ProfileUiState.copy(
    profile: com.zenbuddy.domain.model.UserProfile = this.profile,
    tdee: Double = this.tdee,
    bmi: Double = this.bmi,
    bmr: Double = this.bmr,
    isSaving: Boolean = this.isSaving,
    isLoading: Boolean = this.isLoading,
    saveSuccess: Boolean = this.saveSuccess,
    error: String? = this.error,
    dailyCalorieGoal: Double
): ProfileUiState = ProfileUiState(
    profile = profile.copy(dailyCalorieGoal = dailyCalorieGoal),
    tdee = tdee,
    bmi = bmi,
    bmr = bmr,
    isSaving = isSaving,
    isLoading = isLoading,
    saveSuccess = saveSuccess,
    error = error
)
