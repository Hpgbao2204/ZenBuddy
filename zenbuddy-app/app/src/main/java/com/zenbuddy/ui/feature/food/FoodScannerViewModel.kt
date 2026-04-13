package com.zenbuddy.ui.feature.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class FoodScannerViewModel @Inject constructor(
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FoodScannerUiState())
    val uiState: StateFlow<FoodScannerUiState> = _uiState.asStateFlow()

    private val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

    init {
        loadTodayFood()
    }

    fun onEvent(event: FoodScannerUiEvent) {
        when (event) {
            FoodScannerUiEvent.OpenCamera -> _uiState.update { it.copy(isCameraOpen = true) }
            FoodScannerUiEvent.CloseCamera -> _uiState.update { it.copy(isCameraOpen = false) }
            is FoodScannerUiEvent.PhotoCaptured -> analyzePhoto(event.imageBytes)
            is FoodScannerUiEvent.ConfirmFood -> confirmFood(event.entry)
            FoodScannerUiEvent.DismissAnalysis -> _uiState.update { it.copy(analyzedFood = null) }
            FoodScannerUiEvent.ShowAddDialog -> _uiState.update { it.copy(showAddDialog = true) }
            FoodScannerUiEvent.DismissAddDialog -> _uiState.update { it.copy(showAddDialog = false) }
            is FoodScannerUiEvent.AddManualFood -> addManualFood(event.entry)
            is FoodScannerUiEvent.DeleteFood -> deleteFood(event.id)
            FoodScannerUiEvent.DismissError -> _uiState.update { it.copy(error = null) }
        }
    }

    private fun loadTodayFood() {
        viewModelScope.launch {
            foodRepository.getFoodByDate(today).collect { result ->
                when (result) {
                    is Result.Success -> _uiState.update {
                        it.copy(todayFood = result.data, isLoading = false)
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(error = result.error.message, isLoading = false)
                    }
                    Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
        viewModelScope.launch {
            foodRepository.getTotalCaloriesByDate(today).collect { result ->
                if (result is Result.Success) {
                    _uiState.update { it.copy(totalCalories = result.data) }
                }
            }
        }
    }

    private fun analyzePhoto(imageBytes: ByteArray) {
        _uiState.update { it.copy(isCameraOpen = false, isAnalyzing = true) }
        viewModelScope.launch {
            foodRepository.analyzeFoodImage(imageBytes).collect { result ->
                when (result) {
                    is Result.Success -> _uiState.update {
                        it.copy(analyzedFood = result.data, isAnalyzing = false)
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(error = result.error.message, isAnalyzing = false)
                    }
                    Result.Loading -> _uiState.update { it.copy(isAnalyzing = true) }
                }
            }
        }
    }

    private fun confirmFood(entry: com.zenbuddy.domain.model.FoodEntry) {
        viewModelScope.launch {
            foodRepository.addFood(entry)
            _uiState.update { it.copy(analyzedFood = null) }
        }
    }

    private fun addManualFood(entry: com.zenbuddy.domain.model.FoodEntry) {
        viewModelScope.launch {
            foodRepository.addFood(entry)
            _uiState.update { it.copy(showAddDialog = false) }
        }
    }

    private fun deleteFood(id: String) {
        viewModelScope.launch {
            foodRepository.deleteFood(id)
        }
    }
}
