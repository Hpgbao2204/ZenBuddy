package com.zenbuddy.ui.feature.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.repository.MoodRepository
import com.zenbuddy.domain.usecase.insights.GetMoodInsightsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val moodRepository: MoodRepository,
    private val getMoodInsightsUseCase: GetMoodInsightsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    private var currentDays = 7

    init {
        loadMoods(7)
    }

    fun onEvent(event: InsightsUiEvent) {
        when (event) {
            InsightsUiEvent.LoadInsight -> loadAiInsight()
            is InsightsUiEvent.ChangePeriod -> {
                currentDays = event.days
                loadMoods(event.days)
            }
        }
    }

    private fun loadMoods(days: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMoods = true) }
            moodRepository.getMoodsForPeriod(days).collect { result ->
                when (result) {
                    is Result.Success -> _uiState.update {
                        it.copy(moods = result.data, isLoadingMoods = false)
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(error = result.error.message, isLoadingMoods = false)
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }

    private fun loadAiInsight() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingInsight = true) }
            when (val result = getMoodInsightsUseCase(currentDays)) {
                is Result.Success -> _uiState.update {
                    it.copy(aiInsight = result.data, isLoadingInsight = false)
                }
                is Result.Error -> _uiState.update {
                    it.copy(error = result.error.message, isLoadingInsight = false)
                }
                is Result.Loading -> {}
            }
        }
    }
}
