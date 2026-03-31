package com.zenbuddy.ui.feature.quest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.repository.QuestRepository
import com.zenbuddy.domain.usecase.quest.CompleteQuestUseCase
import com.zenbuddy.domain.usecase.quest.GenerateQuestsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuestViewModel @Inject constructor(
    private val questRepository: QuestRepository,
    private val generateQuestsUseCase: GenerateQuestsUseCase,
    private val completeQuestUseCase: CompleteQuestUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestUiState())
    val uiState: StateFlow<QuestUiState> = _uiState.asStateFlow()

    private val _effects = Channel<QuestEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        loadQuests()
    }

    fun onEvent(event: QuestUiEvent) {
        when (event) {
            QuestUiEvent.GenerateQuests -> generateQuests()
            is QuestUiEvent.CompleteQuest -> completeQuest(event.questId)
        }
    }

    private fun loadQuests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            questRepository.getQuestsForToday().collect { result ->
                when (result) {
                    is Result.Success -> _uiState.update {
                        it.copy(quests = result.data, isLoading = false)
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(error = result.error.message, isLoading = false)
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }

    private fun generateQuests() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, error = null) }
            generateQuestsUseCase().collect { result ->
                when (result) {
                    is Result.Success -> {
                        _uiState.update { it.copy(isGenerating = false) }
                        _effects.send(QuestEffect.ShowSnackbar("Quests generated!"))
                    }
                    is Result.Error -> {
                        _uiState.update {
                            it.copy(isGenerating = false, error = result.error.message)
                        }
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }

    private fun completeQuest(questId: String) {
        viewModelScope.launch {
            when (val result = completeQuestUseCase(questId)) {
                is Result.Success -> {
                    _effects.send(QuestEffect.ShowSnackbar("Quest completed!"))
                }
                is Result.Error -> {
                    _uiState.update { it.copy(error = result.error.message) }
                }
                is Result.Loading -> {}
            }
        }
    }
}
