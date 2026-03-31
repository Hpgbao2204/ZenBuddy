package com.zenbuddy.ui.feature.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.usecase.journal.GetJournalsUseCase
import com.zenbuddy.domain.usecase.journal.SaveJournalUseCase
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
class JournalViewModel @Inject constructor(
    private val getJournalsUseCase: GetJournalsUseCase,
    private val saveJournalUseCase: SaveJournalUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalUiState())
    val uiState: StateFlow<JournalUiState> = _uiState.asStateFlow()

    private val _effects = Channel<JournalEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        loadJournals()
    }

    fun onEvent(event: JournalUiEvent) {
        when (event) {
            is JournalUiEvent.InputChanged -> _uiState.update { it.copy(inputText = event.text) }
            JournalUiEvent.SaveEntry -> saveEntry()
            JournalUiEvent.ToggleRecording -> toggleRecording()
        }
    }

    private fun loadJournals() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getJournalsUseCase().collect { result ->
                when (result) {
                    is Result.Success -> _uiState.update {
                        it.copy(entries = result.data, isLoading = false)
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(error = result.error.message, isLoading = false)
                    }
                    is Result.Loading -> {}
                }
            }
        }
    }

    private fun saveEntry() {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            when (val result = saveJournalUseCase(text)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isSaving = false, inputText = "") }
                    _effects.send(JournalEffect.ShowSnackbar("Entry saved!"))
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isSaving = false, error = result.error.message) }
                }
                is Result.Loading -> {}
            }
        }
    }

    private fun toggleRecording() {
        _uiState.update { it.copy(isRecording = !it.isRecording) }
    }
}
