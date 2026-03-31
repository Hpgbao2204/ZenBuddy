package com.zenbuddy.ui.feature.quest

import com.zenbuddy.domain.model.Quest

data class QuestUiState(
    val quests: List<Quest> = emptyList(),
    val isLoading: Boolean = false,
    val isGenerating: Boolean = false,
    val error: String? = null
)

sealed interface QuestUiEvent {
    data object GenerateQuests : QuestUiEvent
    data class CompleteQuest(val questId: String) : QuestUiEvent
}

sealed interface QuestEffect {
    data class ShowSnackbar(val message: String) : QuestEffect
}
