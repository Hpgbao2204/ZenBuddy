package com.zenbuddy.ui.feature.healthchat

data class HealthChatUiState(
    val messages: List<HealthChatMessage> = emptyList(),
    val currentInput: String = "",
    val isGenerating: Boolean = false,
    val mealPlan: String? = null,
    val workoutPlan: String? = null,
    val isGeneratingPlan: Boolean = false,
    val error: String? = null
)

data class HealthChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

sealed interface HealthChatUiEvent {
    data class InputChanged(val text: String) : HealthChatUiEvent
    data object SendMessage : HealthChatUiEvent
    data object GenerateMealPlan : HealthChatUiEvent
    data object GenerateWorkoutPlan : HealthChatUiEvent
    data object DismissPlan : HealthChatUiEvent
}
