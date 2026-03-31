package com.zenbuddy.ui.feature.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.usecase.mood.LogMoodUseCase
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
class MoodViewModel @Inject constructor(
    private val logMoodUseCase: LogMoodUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoodUiState())
    val uiState: StateFlow<MoodUiState> = _uiState.asStateFlow()

    private val _effects = Channel<MoodEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    fun onEvent(event: MoodUiEvent) {
        when (event) {
            is MoodUiEvent.ScoreChanged -> _uiState.update { it.copy(score = event.score) }
            is MoodUiEvent.NoteChanged -> _uiState.update { it.copy(note = event.note) }
            MoodUiEvent.SaveMood -> saveMood()
        }
    }

    private fun saveMood() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val note = _uiState.value.note.ifBlank { null }
            when (val result = logMoodUseCase(_uiState.value.score, note)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isLoading = false, isSaved = true) }
                    _effects.send(MoodEffect.ShowSnackbar("Mood saved!"))
                    _effects.send(MoodEffect.NavigateBack)
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.error.message) }
                }
                is Result.Loading -> {}
            }
        }
    }
}
