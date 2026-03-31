package com.zenbuddy.ui.feature.breathing

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zenbuddy.ui.theme.Lavender
import com.zenbuddy.ui.theme.LavenderLight
import com.zenbuddy.ui.theme.Mint
import com.zenbuddy.ui.theme.MintLight
import com.zenbuddy.ui.theme.PeachLight

enum class BreathPhase(val label: String, val emoji: String, val durationSec: Int) {
    INHALE("Breathe In", "🌬️", 4),
    HOLD("Hold", "✨", 7),
    EXHALE("Breathe Out", "🫧", 8)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreathingScreen(onNavigateBack: () -> Unit) {
    var isActive by remember { mutableStateOf(false) }
    var currentPhase by remember { mutableStateOf(BreathPhase.INHALE) }
    var cycleCount by remember { mutableIntStateOf(0) }
    val circleScale = remember { Animatable(0.3f) }
    var phaseTimer by remember { mutableIntStateOf(0) }

    // Animation loop
    LaunchedEffect(isActive, currentPhase) {
        if (!isActive) {
            circleScale.snapTo(0.3f)
            return@LaunchedEffect
        }

        val duration = currentPhase.durationSec * 1000

        when (currentPhase) {
            BreathPhase.INHALE -> {
                phaseTimer = currentPhase.durationSec
                circleScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(duration, easing = LinearEasing)
                )
                currentPhase = BreathPhase.HOLD
            }
            BreathPhase.HOLD -> {
                phaseTimer = currentPhase.durationSec
                circleScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(duration, easing = LinearEasing)
                )
                currentPhase = BreathPhase.EXHALE
            }
            BreathPhase.EXHALE -> {
                phaseTimer = currentPhase.durationSec
                circleScale.animateTo(
                    targetValue = 0.3f,
                    animationSpec = tween(duration, easing = LinearEasing)
                )
                cycleCount++
                currentPhase = BreathPhase.INHALE
            }
        }
    }

    // Countdown timer
    LaunchedEffect(isActive, currentPhase) {
        if (!isActive) return@LaunchedEffect
        phaseTimer = currentPhase.durationSec
        repeat(currentPhase.durationSec) {
            kotlinx.coroutines.delay(1000L)
            phaseTimer--
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Breathing 🫧") },
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
                .background(MaterialTheme.colorScheme.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Description
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp, start = 24.dp, end = 24.dp)
            ) {
                Text(
                    text = "4-7-8 Breathing",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "A calming technique to reduce anxiety",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // Animated Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(280.dp)
            ) {
                val scale = circleScale.value
                val primary = Lavender
                val secondary = Mint

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = (size.minDimension / 2) * scale
                    // Outer glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                LavenderLight.copy(alpha = 0.3f),
                                MintLight.copy(alpha = 0.1f),
                                PeachLight.copy(alpha = 0f)
                            ),
                            center = Offset(size.width / 2, size.height / 2),
                            radius = radius * 1.4f
                        ),
                        radius = radius * 1.4f
                    )
                    // Main circle
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(primary.copy(alpha = 0.8f), secondary.copy(alpha = 0.6f)),
                            center = Offset(size.width / 2, size.height / 2),
                            radius = radius
                        ),
                        radius = radius
                    )
                }

                if (isActive) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = currentPhase.emoji,
                            fontSize = 36.sp
                        )
                        Text(
                            text = currentPhase.label,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${phaseTimer.coerceAtLeast(0)}s",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Text(
                        text = "🫧",
                        fontSize = 48.sp
                    )
                }
            }

            // Bottom section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp, start = 24.dp, end = 24.dp)
            ) {
                if (isActive && cycleCount > 0) {
                    Text(
                        text = "Cycle $cycleCount completed ✨",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Button(
                    onClick = {
                        if (isActive) {
                            isActive = false
                            currentPhase = BreathPhase.INHALE
                            cycleCount = 0
                        } else {
                            isActive = true
                            currentPhase = BreathPhase.INHALE
                            cycleCount = 0
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (isActive) "Stop" else "Start Breathing 🌬️",
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Inhale 4s → Hold 7s → Exhale 8s",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
