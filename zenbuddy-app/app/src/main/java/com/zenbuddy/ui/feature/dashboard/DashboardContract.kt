package com.zenbuddy.ui.feature.dashboard

import com.zenbuddy.domain.model.StepCount
import com.zenbuddy.domain.model.WeatherInfo

data class DashboardUiState(
    val todaySteps: StepCount = StepCount(),
    val weeklySteps: List<StepCount> = emptyList(),
    val todayCaloriesIn: Double = 0.0,
    val todayCaloriesBurned: Double = 0.0,
    val stepGoal: Int = 10000,
    val calorieGoal: Double = 2000.0,
    val weather: WeatherInfo? = null,
    val userName: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed interface DashboardUiEvent {
    data object RefreshWeather : DashboardUiEvent
    data object NavigateToSteps : DashboardUiEvent
    data object NavigateToFood : DashboardUiEvent
    data object NavigateToExercises : DashboardUiEvent
    data object NavigateToSchedule : DashboardUiEvent
    data object NavigateToHealthChat : DashboardUiEvent
    data object NavigateToProfile : DashboardUiEvent
}

sealed interface DashboardEffect {
    data object NavigateToSteps : DashboardEffect
    data object NavigateToFood : DashboardEffect
    data object NavigateToExercises : DashboardEffect
    data object NavigateToSchedule : DashboardEffect
    data object NavigateToHealthChat : DashboardEffect
    data object NavigateToProfile : DashboardEffect
}
