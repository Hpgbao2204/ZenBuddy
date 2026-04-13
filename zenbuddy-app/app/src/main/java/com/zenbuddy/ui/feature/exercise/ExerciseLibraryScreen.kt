package com.zenbuddy.ui.feature.exercise

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zenbuddy.domain.model.Exercise

@Composable
fun ExerciseLibraryRoute(
    viewModel: ExerciseViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ExerciseLibraryScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseLibraryScreen(
    uiState: ExerciseLibraryUiState,
    onEvent: (ExerciseUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thư viện bài tập") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Muscle group filter
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedGroup == null,
                    onClick = { onEvent(ExerciseUiEvent.SelectGroup(null)) },
                    label = { Text("Tất cả") }
                )
                uiState.muscleGroups.forEach { group ->
                    FilterChip(
                        selected = uiState.selectedGroup == group,
                        onClick = { onEvent(ExerciseUiEvent.SelectGroup(group)) },
                        label = { Text(getMuscleGroupLabel(group)) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.exercises, key = { it.id }) { exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        onClick = { onEvent(ExerciseUiEvent.SelectExercise(exercise)) }
                    )
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    // Exercise detail bottom sheet
    uiState.selectedExercise?.let { exercise ->
        ModalBottomSheet(
            onDismissRequest = { onEvent(ExerciseUiEvent.DismissDetail) },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            ExerciseDetail(exercise = exercise)
        }
    }
}

@Composable
private fun ExerciseCard(exercise: Exercise, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.FitnessCenter,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = exercise.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = getMuscleGroupLabel(exercise.muscleGroup),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = getDifficultyLabel(exercise.difficulty),
                        style = MaterialTheme.typography.labelSmall,
                        color = getDifficultyColor(exercise.difficulty)
                    )
                    Text(
                        text = "${exercise.durationMinutes} phút",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseDetail(exercise: Exercise) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            text = exercise.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = exercise.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoChip("💪 ${getMuscleGroupLabel(exercise.muscleGroup)}")
            InfoChip("⏱ ${exercise.durationMinutes} phút")
            InfoChip("🔥 ${(exercise.caloriesPerMinute * exercise.durationMinutes).toInt()} kcal")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Hướng dẫn chi tiết",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = exercise.instructions,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
        )
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun InfoChip(text: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium
        )
    }
}

private fun getMuscleGroupLabel(group: String): String = when (group) {
    "chest" -> "Ngực"
    "back" -> "Lưng"
    "legs" -> "Chân"
    "arms" -> "Tay"
    "shoulders" -> "Vai"
    "core" -> "Bụng"
    "cardio" -> "Cardio"
    else -> group.replaceFirstChar { it.uppercase() }
}

private fun getDifficultyLabel(difficulty: String): String = when (difficulty) {
    "beginner" -> "Dễ"
    "intermediate" -> "Trung bình"
    "advanced" -> "Nâng cao"
    else -> difficulty
}

@Composable
private fun getDifficultyColor(difficulty: String) = when (difficulty) {
    "beginner" -> MaterialTheme.colorScheme.primary
    "intermediate" -> MaterialTheme.colorScheme.tertiary
    "advanced" -> MaterialTheme.colorScheme.error
    else -> MaterialTheme.colorScheme.onSurface
}
