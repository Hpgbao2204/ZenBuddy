package com.zenbuddy.ui.feature.mood

data class MoodUiState(
    val score: Int = 5,
    val note: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

sealed interface MoodUiEvent {
    data class ScoreChanged(val score: Int) : MoodUiEvent
    data class NoteChanged(val note: String) : MoodUiEvent
    data object SaveMood : MoodUiEvent
}

sealed interface MoodEffect {
    data object NavigateBack : MoodEffect
    data class ShowSnackbar(val message: String) : MoodEffect
}
