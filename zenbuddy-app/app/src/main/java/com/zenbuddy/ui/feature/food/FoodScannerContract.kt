package com.zenbuddy.ui.feature.food

import com.zenbuddy.domain.model.FoodEntry

data class FoodScannerUiState(
    val todayFood: List<FoodEntry> = emptyList(),
    val totalCalories: Double = 0.0,
    val analyzedFood: FoodEntry? = null,
    val isAnalyzing: Boolean = false,
    val isCameraOpen: Boolean = false,
    val showAddDialog: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed interface FoodScannerUiEvent {
    data object OpenCamera : FoodScannerUiEvent
    data object CloseCamera : FoodScannerUiEvent
    data class PhotoCaptured(val imageBytes: ByteArray) : FoodScannerUiEvent
    data class ConfirmFood(val entry: FoodEntry) : FoodScannerUiEvent
    data object DismissAnalysis : FoodScannerUiEvent
    data object ShowAddDialog : FoodScannerUiEvent
    data object DismissAddDialog : FoodScannerUiEvent
    data class AddManualFood(val entry: FoodEntry) : FoodScannerUiEvent
    data class DeleteFood(val id: String) : FoodScannerUiEvent
}
