package com.zenbuddy.ui.feature.chat

import com.zenbuddy.domain.model.ChatMessage

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val streamingText: String = "",
    val isGenerating: Boolean = false,
    val error: String? = null
)

sealed interface ChatUiEvent {
    data class InputChanged(val text: String) : ChatUiEvent
    data class Send(val text: String) : ChatUiEvent
    data object CancelGeneration : ChatUiEvent
}

sealed interface ChatEffect {
    data class ShowSnackbar(val message: String) : ChatEffect
}
