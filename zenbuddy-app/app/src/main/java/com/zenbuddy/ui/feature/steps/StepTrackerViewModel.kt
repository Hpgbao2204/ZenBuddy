package com.zenbuddy.ui.feature.steps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenbuddy.core.result.Result
import com.zenbuddy.data.sensor.StepSensorManager
import com.zenbuddy.domain.repository.StepRepository
import com.zenbuddy.domain.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class StepTrackerViewModel @Inject constructor(
    private val stepRepository: StepRepository,
    private val userProfileRepository: UserProfileRepository,
    private val stepSensorManager: StepSensorManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(StepTrackerUiState())
    val uiState: StateFlow<StepTrackerUiState> = _uiState.asStateFlow()

    private val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    private var initialStepCount: Int? = null
    private var sensorStarted = false

    init {
        _uiState.update { it.copy(sensorAvailable = stepSensorManager.isAvailable) }
        loadProfile()
        observeSteps()
    }

    fun onEvent(event: StepTrackerUiEvent) {
        when (event) {
            StepTrackerUiEvent.Refresh -> loadProfile()
            StepTrackerUiEvent.PermissionGranted -> observeSensor()
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val result = userProfileRepository.getProfile().first()
            if (result is Result.Success) {
                _uiState.update { it.copy(stepGoal = result.data.dailyStepGoal) }
            }
        }
    }

    private fun observeSteps() {
        viewModelScope.launch {
            stepRepository.getTodaySteps().collect { result ->
                when (result) {
                    is Result.Success -> _uiState.update {
                        it.copy(todaySteps = result.data, isLoading = false)
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(error = result.error.message, isLoading = false)
                    }
                    Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
        viewModelScope.launch {
            stepRepository.getWeeklySteps().collect { result ->
                if (result is Result.Success) {
                    _uiState.update { it.copy(weeklySteps = result.data) }
                }
            }
        }
    }

    private fun observeSensor() {
        if (sensorStarted) return
        sensorStarted = true
        viewModelScope.launch {
            stepSensorManager.observeSteps().collect { totalSteps ->
                if (initialStepCount == null) {
                    initialStepCount = totalSteps - (_uiState.value.todaySteps.steps)
                }
                val todaySteps = totalSteps - (initialStepCount ?: totalSteps)
                val profile = userProfileRepository.getProfile().first()
                val weight = if (profile is Result.Success) profile.data.weightKg else 70.0
                stepRepository.updateSteps(today, todaySteps.coerceAtLeast(0), weight)
            }
        }
    }
}
