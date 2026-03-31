package com.zenbuddy.ui.feature.games

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class GameType { NONE, BUBBLE_POP, COLOR_TAP, ZEN_MEMORY }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GamesScreen(
    onNavigateBack: () -> Unit
) {
    var currentGame by remember { mutableStateOf(GameType.NONE) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            when (currentGame) {
                                GameType.NONE -> "Mini Games 🎮"
                                GameType.BUBBLE_POP -> "Bubble Pop 🫧"
                                GameType.COLOR_TAP -> "Color Tap 🎨"
                                GameType.ZEN_MEMORY -> "Zen Memory 🧠"
                            },
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "Simple games to ease stress",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentGame != GameType.NONE) currentGame = GameType.NONE
                        else onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentGame) {
                GameType.NONE -> GameMenu(
                    onSelectGame = { currentGame = it }
                )
                GameType.BUBBLE_POP -> BubblePopGame()
                GameType.COLOR_TAP -> ColorTapGame()
                GameType.ZEN_MEMORY -> ZenMemoryGame()
            }
        }
    }
}

// ============== Game Menu ==============

@Composable
private fun GameMenu(onSelectGame: (GameType) -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "menuFloat")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )

    val animItems = remember { List(3) { Animatable(0f) } }
    LaunchedEffect(Unit) {
        animItems.forEachIndexed { i, anim ->
            delay(i * 100L)
            anim.animateTo(1f, tween(400, easing = FastOutSlowInEasing))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🎮",
                    fontSize = 48.sp,
                    modifier = Modifier.graphicsLayer { translationY = floatOffset }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Take a break",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Simple games to reduce stress and anxiety",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }

        // Game cards
        GameCard(
            emoji = "🫧",
            title = "Bubble Pop",
            description = "Tap bubbles before they float away. Satisfying and calming.",
            modifier = Modifier.graphicsLayer {
                alpha = animItems[0].value
                translationY = (1f - animItems[0].value) * 40f
            },
            onClick = { onSelectGame(GameType.BUBBLE_POP) }
        )

        GameCard(
            emoji = "🎨",
            title = "Color Tap",
            description = "Tap the matching color as fast as you can. Focus your mind.",
            modifier = Modifier.graphicsLayer {
                alpha = animItems[1].value
                translationY = (1f - animItems[1].value) * 40f
            },
            onClick = { onSelectGame(GameType.COLOR_TAP) }
        )

        GameCard(
            emoji = "🧠",
            title = "Zen Memory",
            description = "Match pairs of zen emojis. Train your memory gently.",
            modifier = Modifier.graphicsLayer {
                alpha = animItems[2].value
                translationY = (1f - animItems[2].value) * 40f
            },
            onClick = { onSelectGame(GameType.ZEN_MEMORY) }
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun GameCard(
    emoji: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 28.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text("→", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ============== Bubble Pop Game ==============

data class Bubble(
    val id: Int,
    val x: Float,
    val y: Float,
    val size: Float,
    val emoji: String,
    val popped: Boolean = false
)

@Composable
private fun BubblePopGame() {
    var score by remember { mutableIntStateOf(0) }
    var bubbles by remember { mutableStateOf(generateBubbles()) }
    var round by remember { mutableIntStateOf(1) }
    var gameComplete by remember { mutableStateOf(false) }

    val allPopped = bubbles.all { it.popped }

    LaunchedEffect(allPopped) {
        if (allPopped && bubbles.isNotEmpty()) {
            delay(500)
            if (round < 5) {
                round++
                bubbles = generateBubbles(count = 8 + round * 2)
            } else {
                gameComplete = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Score
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Score: $score",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Round $round/5",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (gameComplete) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🎉", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Amazing!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Final Score: $score",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        score = 0
                        round = 1
                        gameComplete = false
                        bubbles = generateBubbles()
                    },
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Play Again")
                }
            }
        } else {
            // Bubble grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(bubbles, key = { it.id }) { bubble ->
                    AnimatedVisibility(
                        visible = !bubble.popped,
                        enter = scaleIn(spring(stiffness = Spring.StiffnessLow)),
                        exit = scaleOut(tween(200)) + fadeOut(tween(200))
                    ) {
                        val scale by animateFloatAsState(
                            targetValue = if (bubble.popped) 0f else 1f,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "pop"
                        )
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(
                                    MaterialTheme.colorScheme.primaryContainer.copy(
                                        alpha = 0.6f + (bubble.size * 0.4f)
                                    )
                                )
                                .clickable {
                                    if (!bubble.popped) {
                                        score += (bubble.size * 10).toInt() + 5
                                        bubbles = bubbles.map {
                                            if (it.id == bubble.id) it.copy(popped = true) else it
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = bubble.emoji,
                                fontSize = (20 + bubble.size * 16).sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun generateBubbles(count: Int = 12): List<Bubble> {
    val emojis = listOf("🫧", "💫", "✨", "🌸", "🦋", "🌊", "💜", "🌟")
    return List(count) { i ->
        Bubble(
            id = i,
            x = (Math.random() * 0.8f + 0.1f).toFloat(),
            y = (Math.random() * 0.8f + 0.1f).toFloat(),
            size = (Math.random() * 0.6f + 0.4f).toFloat(),
            emoji = emojis.random()
        )
    }
}

// ============== Color Tap Game ==============

data class ColorItem(
    val color: Color,
    val name: String,
    val emoji: String
)

private val gameColors = listOf(
    ColorItem(Color(0xFFE57373), "Red", "🔴"),
    ColorItem(Color(0xFF81C784), "Green", "🟢"),
    ColorItem(Color(0xFF64B5F6), "Blue", "🔵"),
    ColorItem(Color(0xFFFFB74D), "Orange", "🟠"),
    ColorItem(Color(0xFFBA68C8), "Purple", "🟣"),
    ColorItem(Color(0xFFFFD54F), "Yellow", "🟡")
)

@Composable
private fun ColorTapGame() {
    var score by remember { mutableIntStateOf(0) }
    var targetColor by remember { mutableStateOf(gameColors.random()) }
    var options by remember { mutableStateOf(generateColorOptions(targetColor)) }
    var streak by remember { mutableIntStateOf(0) }
    var timeLeft by remember { mutableIntStateOf(30) }
    var gameStarted by remember { mutableStateOf(false) }
    var gameOver by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(gameStarted) {
        if (gameStarted) {
            while (timeLeft > 0) {
                delay(1000)
                timeLeft--
            }
            gameOver = true
        }
    }

    LaunchedEffect(feedback) {
        if (feedback != null) {
            delay(400)
            feedback = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!gameStarted && !gameOver) {
            // Start screen
            Text("🎨", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Tap the matching color!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "30 seconds. How high can you score?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { gameStarted = true },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("Start!", fontSize = 18.sp)
            }
        } else if (gameOver) {
            // Game over
            Text("⏰", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Time's up!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Score: $score | Best streak: $streak",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    score = 0
                    streak = 0
                    timeLeft = 30
                    gameOver = false
                    gameStarted = true
                    targetColor = gameColors.random()
                    options = generateColorOptions(targetColor)
                },
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Play Again")
            }
        } else {
            // Active game
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Score: $score",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "🔥 $streak",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    "⏰ ${timeLeft}s",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (timeLeft <= 5) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Target
            Text(
                text = "Tap",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = targetColor.name,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = targetColor.color
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Feedback
            AnimatedVisibility(
                visible = feedback != null,
                enter = fadeIn(tween(100)),
                exit = fadeOut(tween(200))
            ) {
                Text(
                    text = feedback ?: "",
                    fontSize = 32.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Color options
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(options) { color ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(color.color)
                            .clickable {
                                if (color == targetColor) {
                                    score += 10 + streak * 2
                                    streak++
                                    feedback = "✅"
                                } else {
                                    streak = 0
                                    feedback = "❌"
                                }
                                targetColor = gameColors.random()
                                options = generateColorOptions(targetColor)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(color.emoji, fontSize = 32.sp)
                    }
                }
            }
        }
    }
}

private fun generateColorOptions(target: ColorItem): List<ColorItem> {
    val others = gameColors.filter { it != target }.shuffled().take(5)
    return (others + target).shuffled()
}

// ============== Zen Memory Game ==============

data class MemoryCard(
    val id: Int,
    val emoji: String,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)

@Composable
private fun ZenMemoryGame() {
    val emojis = listOf("🌸", "🦋", "🌊", "🧘", "🌿", "💜", "🕊️", "🌙")
    var cards by remember { mutableStateOf(generateMemoryCards(emojis)) }
    var firstFlipped by remember { mutableStateOf<Int?>(null) }
    var secondFlipped by remember { mutableStateOf<Int?>(null) }
    var moves by remember { mutableIntStateOf(0) }
    var pairs by remember { mutableIntStateOf(0) }
    var isChecking by remember { mutableStateOf(false) }

    val totalPairs = emojis.size
    val gameComplete = pairs == totalPairs

    // Check for match
    LaunchedEffect(secondFlipped) {
        if (firstFlipped != null && secondFlipped != null) {
            isChecking = true
            delay(600)
            val first = cards[firstFlipped!!]
            val second = cards[secondFlipped!!]
            if (first.emoji == second.emoji) {
                cards = cards.map {
                    if (it.id == firstFlipped || it.id == secondFlipped)
                        it.copy(isMatched = true)
                    else it
                }
                pairs++
            } else {
                cards = cards.map {
                    if (it.id == firstFlipped || it.id == secondFlipped)
                        it.copy(isFlipped = false)
                    else it
                }
            }
            firstFlipped = null
            secondFlipped = null
            isChecking = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Pairs: $pairs/$totalPairs",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "Moves: $moves",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (gameComplete) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🧠✨", fontSize = 64.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Well done!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Completed in $moves moves",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        cards = generateMemoryCards(emojis)
                        firstFlipped = null
                        secondFlipped = null
                        moves = 0
                        pairs = 0
                    },
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Play Again")
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cards, key = { it.id }) { card ->
                    val flipScale by animateFloatAsState(
                        targetValue = if (card.isFlipped || card.isMatched) 1f else 0.95f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "flip"
                    )
                    Card(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .scale(flipScale)
                            .clickable(enabled = !isChecking && !card.isFlipped && !card.isMatched) {
                                if (firstFlipped == null) {
                                    firstFlipped = card.id
                                    cards = cards.map {
                                        if (it.id == card.id) it.copy(isFlipped = true) else it
                                    }
                                    moves++
                                } else if (secondFlipped == null && card.id != firstFlipped) {
                                    secondFlipped = card.id
                                    cards = cards.map {
                                        if (it.id == card.id) it.copy(isFlipped = true) else it
                                    }
                                    moves++
                                }
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                card.isMatched -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                card.isFlipped -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (card.isFlipped || card.isMatched) 4.dp else 1.dp
                        )
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (card.isFlipped || card.isMatched) {
                                Text(card.emoji, fontSize = 28.sp)
                            } else {
                                Text("✦", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun generateMemoryCards(emojis: List<String>): List<MemoryCard> {
    val pairs = emojis.flatMap { emoji -> listOf(emoji, emoji) }
    return pairs.shuffled().mapIndexed { index, emoji ->
        MemoryCard(id = index, emoji = emoji)
    }
}
