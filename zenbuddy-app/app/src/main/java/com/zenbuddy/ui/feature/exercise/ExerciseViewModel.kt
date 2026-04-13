package com.zenbuddy.ui.feature.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.repository.ExerciseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExerciseViewModel @Inject constructor(
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseLibraryUiState())
    val uiState: StateFlow<ExerciseLibraryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { exerciseRepository.seedIfEmpty() }
        loadMuscleGroups()
        loadExercises()
    }

    fun onEvent(event: ExerciseUiEvent) {
        when (event) {
            is ExerciseUiEvent.SelectGroup -> {
                _uiState.update { it.copy(selectedGroup = event.group) }
                if (event.group != null) loadByGroup(event.group)
                else loadExercises()
            }
            is ExerciseUiEvent.SelectExercise -> {
                _uiState.update { it.copy(selectedExercise = event.exercise) }
            }
            ExerciseUiEvent.DismissDetail -> {
                _uiState.update { it.copy(selectedExercise = null) }
            }
        }
    }

    private fun loadMuscleGroups() {
        viewModelScope.launch {
            exerciseRepository.getMuscleGroups().collect { result ->
                if (result is Result.Success) {
                    _uiState.update { it.copy(muscleGroups = result.data) }
                }
            }
        }
    }

    private fun loadExercises() {
        viewModelScope.launch {
            exerciseRepository.getAllExercises().collect { result ->
                when (result) {
                    is Result.Success -> _uiState.update {
                        it.copy(exercises = result.data, isLoading = false)
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(error = result.error.message, isLoading = false)
                    }
                    Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                }
            }
        }
    }

    private fun loadByGroup(group: String) {
        viewModelScope.launch {
            exerciseRepository.getByMuscleGroup(group).collect { result ->
                if (result is Result.Success) {
                    _uiState.update { it.copy(exercises = result.data) }
                }
            }
        }
    }
}
