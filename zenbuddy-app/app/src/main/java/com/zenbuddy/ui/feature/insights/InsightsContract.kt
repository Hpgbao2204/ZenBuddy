package com.zenbuddy.ui.feature.insights

import com.zenbuddy.domain.model.MoodEntry

data class InsightsUiState(
    val moods: List<MoodEntry> = emptyList(),
    val aiInsight: String? = null,
    val isLoadingMoods: Boolean = true,
    val isLoadingInsight: Boolean = false,
    val error: String? = null
)

sealed interface InsightsUiEvent {
    data object LoadInsight : InsightsUiEvent
    data class ChangePeriod(val days: Int) : InsightsUiEvent
}
