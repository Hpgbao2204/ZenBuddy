package com.zenbuddy.ui.feature.home

import com.zenbuddy.domain.model.MoodEntry

data class HomeUiState(
    val todayMood: MoodEntry? = null,
    val activeQuestCount: Int = 0,
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed interface HomeUiEvent {
    data object Refresh : HomeUiEvent
}

sealed interface HomeEffect {
    data object NavigateToMood : HomeEffect
    data object NavigateToChat : HomeEffect
    data object NavigateToQuests : HomeEffect
}
