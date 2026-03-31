# Jetpack Compose M3 — ZenBuddy UI Guide

## Screen Anatomy

Every screen follows the same pattern — **no state in Composables**, always stateless + hoisted.

```kotlin
// Stateful entry point (connect to ViewModel)
@Composable
fun MoodRoute(
    viewModel: MoodViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                MoodEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    MoodScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent
    )
}

// Stateless, previewable
@Composable
fun MoodScreen(
    uiState: MoodUiState,
    onEvent: (MoodUiEvent) -> Unit
) {
    Scaffold(
        topBar = { ZenTopBar(title = "How are you feeling?") }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MoodSlider(
                value = uiState.score.toFloat(),
                onValueChange = { onEvent(MoodUiEvent.ScoreChanged(it.toInt())) }
            )
            if (uiState.isLoading) CircularProgressIndicator()
            uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        }
    }
}
```

## Performance Rules

| Rule | Implementation |
|---|---|
| No heavy work in Composable body | Move to `remember { }` or ViewModel |
| Stable lists in LazyColumn | Use `key` param + `@Stable` / `@Immutable` data classes |
| Derived state | `val filtered by remember { derivedStateOf { list.filter { it.done } } }` |
| Image loading | Use Coil `AsyncImage` — never load bitmaps manually |

```kotlin
// GOOD — stable key prevents unnecessary recompositions
LazyColumn {
    items(quests, key = { it.id }) { quest ->
        QuestItem(quest = quest, onComplete = { onEvent(QuestEvent.Complete(quest.id)) })
    }
}

// BAD — no key, full recomposition on any list change
LazyColumn {
    items(quests) { quest -> QuestItem(quest) }
}
```

## Material 3 Theme Setup

```kotlin
// ui/theme/Theme.kt
@Composable
fun ZenBuddyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) darkColorScheme(
        primary = ZenGreen,
        secondary = CalmBlue,
        background = DarkSurface
    ) else lightColorScheme(
        primary = ZenGreen,
        secondary = CalmBlue
    )
    MaterialTheme(colorScheme = colorScheme, typography = ZenTypography, content = content)
}
```

## Navigation Setup

```kotlin
// ui/navigation/NavGraph.kt
@Composable
fun ZenNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = Route.Home.path) {
        composable(Route.Home.path) {
            HomeRoute(onNavigateToMood = { navController.navigate(Route.Mood.path) })
        }
        composable(Route.Mood.path) {
            MoodRoute(onNavigateToHome = { navController.popBackStack() })
        }
        composable(Route.Chat.path) {
            ChatRoute()
        }
        composable(Route.Quests.path) {
            QuestRoute()
        }
    }
}

sealed class Route(val path: String) {
    data object Home : Route("home")
    data object Mood : Route("mood")
    data object Chat : Route("chat")
    data object Quests : Route("quests")
}
```

## Mood Tracking UI — Full Example

```kotlin
@Composable
fun MoodSlider(value: Float, onValueChange: (Float) -> Unit) {
    val emojis = listOf("😞", "😟", "😐", "🙂", "😊", "😄", "🤩")
    val index = (value / 10f * (emojis.size - 1)).toInt().coerceIn(0, emojis.size - 1)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emojis[index], style = MaterialTheme.typography.displayLarge)
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
```

## Streaming Chat UI (AI Companion)

```kotlin
@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isFromUser) Alignment.End else Alignment.Start
    val bubbleColor = if (message.isFromUser)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = alignment) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = bubbleColor,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            // message.text updates token-by-token from StateFlow
            Text(
                text = message.text,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
```

## Previews (always add)

```kotlin
@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun MoodScreenPreview() {
    ZenBuddyTheme {
        MoodScreen(
            uiState = MoodUiState(score = 7),
            onEvent = {}
        )
    }
}
```
