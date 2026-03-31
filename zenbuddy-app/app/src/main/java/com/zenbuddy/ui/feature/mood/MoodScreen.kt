package com.zenbuddy.ui.feature.mood

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MoodRoute(
    viewModel: MoodViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                MoodEffect.NavigateBack -> onNavigateBack()
                is MoodEffect.ShowSnackbar -> {}
            }
        }
    }

    MoodScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodScreen(
    uiState: MoodUiState,
    onEvent: (MoodUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("How are you feeling?") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                MoodSlider(
                    value = uiState.score.toFloat(),
                    onValueChange = { onEvent(MoodUiEvent.ScoreChanged(it.toInt())) }
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.note,
                    onValueChange = { onEvent(MoodUiEvent.NoteChanged(it)) },
                    label = { Text("Add a note (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onEvent(MoodUiEvent.SaveMood) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.padding(end = 8.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Text("Save Mood")
                }

                uiState.error?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun MoodSlider(value: Float, onValueChange: (Float) -> Unit) {
    val emojis = listOf("😞", "😟", "😐", "🙂", "😊", "😄", "🤩")
    val index = ((value / 10f) * (emojis.size - 1)).toInt().coerceIn(0, emojis.size - 1)
    val emojiScale = remember { Animatable(1f) }

    LaunchedEffect(index) {
        emojiScale.animateTo(1.3f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        emojiScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = emojis[index],
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.scale(emojiScale.value)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 1f..10f,
            steps = 8,
            modifier = Modifier.fillMaxWidth()
        )
        Text("${value.toInt()} / 10", style = MaterialTheme.typography.bodyMedium)
    }
}
