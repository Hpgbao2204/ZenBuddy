package com.zenbuddy.ui.feature.steps

import com.zenbuddy.domain.model.StepCount

data class StepTrackerUiState(
    val todaySteps: StepCount = StepCount(),
    val weeklySteps: List<StepCount> = emptyList(),
    val stepGoal: Int = 10000,
    val sensorAvailable: Boolean = true,
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed interface StepTrackerUiEvent {
    data object Refresh : StepTrackerUiEvent
}
