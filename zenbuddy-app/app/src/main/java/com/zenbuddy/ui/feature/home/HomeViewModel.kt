package com.zenbuddy.ui.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.repository.MoodRepository
import com.zenbuddy.domain.repository.QuestRepository
import com.zenbuddy.domain.usecase.insights.GetDailyAffirmationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val moodRepository: MoodRepository,
    private val questRepository: QuestRepository,
    private val getDailyAffirmationUseCase: GetDailyAffirmationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(greeting = buildGreeting()) }
        loadDashboard()
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            HomeUiEvent.Refresh -> loadDashboard()
            HomeUiEvent.LoadAffirmation -> loadAffirmation()
        }
    }

    private fun buildGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good morning 🌅"
            hour < 17 -> "Good afternoon ☀️"
            hour < 21 -> "Good evening 🌙"
            else -> "Sweet dreams 🌜"
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

    private fun loadAffirmation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingAffirmation = true) }
            when (val result = getDailyAffirmationUseCase()) {
                is Result.Success -> _uiState.update {
                    it.copy(affirmation = result.data, isLoadingAffirmation = false)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoadingAffirmation = false)
                }
                is Result.Loading -> {}
            }
        }
    }
}
