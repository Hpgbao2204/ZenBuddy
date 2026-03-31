package com.zenbuddy.ui.feature.journal

import com.zenbuddy.domain.model.JournalEntry

data class JournalUiState(
    val entries: List<JournalEntry> = emptyList(),
    val inputText: String = "",
    val isRecording: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

sealed interface JournalUiEvent {
    data class InputChanged(val text: String) : JournalUiEvent
    data object SaveEntry : JournalUiEvent
    data object ToggleRecording : JournalUiEvent
}

sealed interface JournalEffect {
    data class ShowSnackbar(val message: String) : JournalEffect
}
