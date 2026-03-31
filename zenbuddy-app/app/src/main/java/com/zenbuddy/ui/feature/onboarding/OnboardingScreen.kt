package com.zenbuddy.ui.feature.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String
)

private val pages = listOf(
    OnboardingPage(
        emoji = "🌸",
        title = "Welcome to ZenBuddy",
        description = "Your gentle companion for mental wellness. Track your mood, journal your thoughts, and find peace — one day at a time."
    ),
    OnboardingPage(
        emoji = "🤖💜",
        title = "AI-Powered Support",
        description = "Chat with your personal AI buddy who remembers your journey. Get personalized affirmations, mood insights, and gentle healing quests."
    ),
    OnboardingPage(
        emoji = "🫧",
        title = "Breathe & Be Present",
        description = "Guided breathing exercises, daily check-ins, and micro-tasks designed to bring calm to your everyday life."
    )
)

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Skip button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            if (pagerState.currentPage < pages.size - 1) {
                TextButton(onClick = onFinished) {
                    Text("Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            OnboardingPageContent(pages[page])
        }

        // Dots indicator
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 24.dp)
        ) {
            val dotTransition = rememberInfiniteTransition(label = "dotPulse")
            val dotScale by dotTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dotScale"
            )
            repeat(pages.size) { index ->
                val isCurrent = index == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .size(if (isCurrent) 12.dp else 8.dp)
                        .graphicsLayer {
                            if (isCurrent) {
                                scaleX = dotScale
                                scaleY = dotScale
                            }
                        }
                        .clip(CircleShape)
                        .background(
                            if (isCurrent)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.primaryContainer
                        )
                )
            }
        }

        // Button
        Button(
            onClick = {
                if (pagerState.currentPage < pages.size - 1) {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                } else {
                    onFinished()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = if (pagerState.currentPage < pages.size - 1) "Next" else "Let's Begin 🌸",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -20f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "emojiFloat"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInHorizontally()
        ) {
            Text(
                text = page.emoji,
                fontSize = 80.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { translationY = floatOffset }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
