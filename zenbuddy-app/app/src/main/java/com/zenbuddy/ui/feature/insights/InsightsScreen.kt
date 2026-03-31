package com.zenbuddy.ui.feature.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zenbuddy.domain.model.MoodEntry
import com.zenbuddy.ui.theme.MoodGood
import com.zenbuddy.ui.theme.MoodGreat
import com.zenbuddy.ui.theme.MoodLow
import com.zenbuddy.ui.theme.MoodNeutral
import com.zenbuddy.ui.theme.MoodSad
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InsightsRoute(
    viewModel: InsightsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    InsightsScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    uiState: InsightsUiState,
    onEvent: (InsightsUiEvent) -> Unit,
    onNavigateBack: () -> Unit
) {
    var selectedDays by remember { mutableIntStateOf(7) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mood Insights 📊") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Period selector chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf(7 to "7 days", 14 to "14 days", 30 to "30 days").forEach { (days, label) ->
                    FilterChip(
                        selected = selectedDays == days,
                        onClick = {
                            selectedDays = days
                            onEvent(InsightsUiEvent.ChangePeriod(days))
                        },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mood visual chart
            if (uiState.isLoadingMoods) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (uiState.moods.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📝", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No mood data yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Start logging your mood to see insights here!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Emoji bar chart
                MoodBarChart(moods = uiState.moods)

                // Stats summary
                val avgScore = uiState.moods.map { it.score }.average()
                val trend = if (uiState.moods.size >= 3) {
                    val recent = uiState.moods.takeLast(3).map { it.score }.average()
                    val earlier = uiState.moods.take(3).map { it.score }.average()
                    when {
                        recent > earlier + 0.5 -> "📈 Trending up"
                        recent < earlier - 0.5 -> "📉 Trending down"
                        else -> "➡️ Staying steady"
                    }
                } else "📊 Need more data"

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard("Average", String.format("%.1f", avgScore), moodEmoji(avgScore.toInt()))
                    StatCard("Entries", "${uiState.moods.size}", "📝")
                    StatCard("Trend", trend.substringAfter(" "), trend.substringBefore(" "))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AI Insight section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Insight ✨",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (uiState.isLoadingInsight) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(20.dp).width(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Analyzing your mood patterns...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else if (uiState.aiInsight != null) {
                        Text(
                            text = uiState.aiInsight,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        OutlinedButton(
                            onClick = { onEvent(InsightsUiEvent.LoadInsight) },
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Get AI Analysis")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MoodBarChart(moods: List<MoodEntry>) {
    val dateFormat = remember { SimpleDateFormat("dd/MM", Locale.getDefault()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Your Mood Journey",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(moods, key = { it.id }) { mood ->
                    MoodBarItem(
                        score = mood.score,
                        date = dateFormat.format(Date(mood.createdAt))
                    )
                }
            }
        }
    }
}

@Composable
private fun MoodBarItem(score: Int, date: String) {
    val color = when {
        score <= 2 -> MoodSad
        score <= 4 -> MoodLow
        score <= 6 -> MoodNeutral
        score <= 8 -> MoodGood
        else -> MoodGreat
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(44.dp)
    ) {
        Text(text = moodEmoji(score), fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(28.dp)
                .height((score * 12 + 12).dp)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(color.copy(alpha = 0.7f))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = date,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatCard(label: String, value: String, emoji: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun moodEmoji(score: Int): String = when {
    score <= 1 -> "😢"
    score <= 3 -> "😟"
    score <= 5 -> "😐"
    score <= 7 -> "🙂"
    score <= 8 -> "😊"
    score <= 9 -> "😄"
    else -> "🤩"
}
