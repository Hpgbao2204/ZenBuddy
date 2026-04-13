package com.zenbuddy.ui.feature.exercise

import com.zenbuddy.domain.model.Exercise

data class ExerciseLibraryUiState(
    val exercises: List<Exercise> = emptyList(),
    val muscleGroups: List<String> = emptyList(),
    val selectedGroup: String? = null,
    val selectedExercise: Exercise? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed interface ExerciseUiEvent {
    data class SelectGroup(val group: String?) : ExerciseUiEvent
    data class SelectExercise(val exercise: Exercise) : ExerciseUiEvent
    data object DismissDetail : ExerciseUiEvent
}
