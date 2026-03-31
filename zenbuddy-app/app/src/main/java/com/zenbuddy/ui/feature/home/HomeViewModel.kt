package com.zenbuddy.ui.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.repository.MoodRepository
import com.zenbuddy.domain.repository.QuestRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val moodRepository: MoodRepository,
    private val questRepository: QuestRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.Refresh -> loadDashboard()
        }
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            launch {
                moodRepository.getTodayMood().collect { result ->
                    when (result) {
                        is Result.Success -> _uiState.update {
                            it.copy(todayMood = result.data, isLoading = false)
                        }
                        is Result.Error -> _uiState.update {
                            it.copy(error = result.error.message, isLoading = false)
                        }
                        is Result.Loading -> {}
                    }
                }
            }

            launch {
                questRepository.getActiveQuestCount().collect { result ->
                    when (result) {
                        is Result.Success -> _uiState.update {
                            it.copy(activeQuestCount = result.data)
                        }
                        is Result.Error -> {}
                        is Result.Loading -> {}
                    }
                }
            }
        }
    }
}
