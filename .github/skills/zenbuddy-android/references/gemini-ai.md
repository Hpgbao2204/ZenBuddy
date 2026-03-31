# Gemini AI Integration — ZenBuddy

## Dependency

```kotlin
// build.gradle.kts (app)
dependencies {
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")
}
```

## Prompt Structure (never skip any section)

```
[SYSTEM_ROLE]
You are ZenBuddy, an empathetic and non-prescriptive mental health companion.
You listen, validate feelings, and suggest gentle micro-actions.
Never diagnose, prescribe medication, or replace professional therapy.
Always end with a warm, optional 1-sentence check-in question.

[SAFETY]
If the user expresses thoughts of self-harm or suicide, respond with:
"I hear you, and I care about your safety. Please reach out to a mental health
professional or crisis line immediately. You are not alone."
Do not continue the conversation until the user acknowledges the message.

[USER_CONTEXT]
Recent mood scores (last 7 days): {{moodHistory}}
Recent journal entries: {{journalSummary}}

[GOAL]
Validate the user's current feeling: "{{currentMessage}}"
Then suggest exactly 1 gentle, actionable micro-task relevant to their mood.
```

## Repository — Streaming Chat

```kotlin
// domain/repository/ChatRepository.kt
interface ChatRepository {
    fun sendMessage(userMessage: String, context: ChatContext): Flow<Result<String>>
    fun generateQuests(moodScore: Int, recentJournal: String): Flow<Result<List<String>>>
}

// data/repository/ChatRepositoryImpl.kt
class ChatRepositoryImpl @Inject constructor(
    private val moodDao: MoodDao,
    private val journalDao: JournalDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ChatRepository {

    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f
            topP = 0.9f
            maxOutputTokens = 512
        }
    )

    override fun sendMessage(
        userMessage: String,
        context: ChatContext
    ): Flow<Result<String>> = flow {
        emit(Result.Loading)
        val prompt = buildPrompt(userMessage, context)
        val responseBuilder = StringBuilder()
        model.generateContentStream(prompt).collect { chunk ->
            val token = chunk.text ?: return@collect
            responseBuilder.append(token)
            emit(Result.Success(responseBuilder.toString()))   // partial text each token
        }
    }.catch { e ->
        emit(Result.Error(AppError.AiError(e.message ?: "Gemini error")))
    }.flowOn(ioDispatcher)

    private fun buildPrompt(userMessage: String, context: ChatContext): String = """
        [SYSTEM_ROLE]
        You are ZenBuddy, an empathetic non-prescriptive mental health companion.
        Never diagnose or prescribe. Suggest only gentle micro-actions.

        [SAFETY]
        If the user mentions self-harm or suicide, direct them to a crisis line immediately.

        [USER_CONTEXT]
        Recent mood scores: ${context.moodHistory.joinToString(", ")}
        Recent journals: ${context.journalSummary}

        [GOAL]
        Validate and respond to: "$userMessage"
        Suggest 1 gentle micro-task. End with a warm check-in question.
    """.trimIndent()

    override fun generateQuests(
        moodScore: Int,
        recentJournal: String
    ): Flow<Result<List<String>>> = flow {
        emit(Result.Loading)
        val prompt = """
            The user's current mood score is $moodScore/10.
            Recent journal: "$recentJournal"
            Generate exactly 3 short, actionable, gentle micro-tasks to improve their mood.
            Output ONLY a JSON array of strings. Example: ["Task 1", "Task 2", "Task 3"]
        """.trimIndent()

        val response = model.generateContent(prompt)
        val json = response.text ?: throw Exception("Empty response")
        val tasks = parseTasksJson(json)
        emit(Result.Success(tasks))
    }.catch { e ->
        emit(Result.Error(AppError.AiError(e.message ?: "Quest generation failed")))
    }.flowOn(ioDispatcher)

    private fun parseTasksJson(raw: String): List<String> {
        // minimal parsing — extract content between [ ]
        val cleaned = raw.substringAfter("[").substringBefore("]")
        return cleaned.split(",").map { it.trim().removeSurrounding("\"") }
    }
}
```

## ChatViewModel — Streaming State

```kotlin
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isGenerating: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val sendMessage: SendMessageUseCase,
    private val generateQuests: GenerateQuestsUseCase,
    private val chatDao: ChatDao,
    private val moodDao: MoodDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var streamingJob: Job? = null

    fun onEvent(event: ChatUiEvent) {
        when (event) {
            is ChatUiEvent.Send -> handleSend(event.text)
            ChatUiEvent.CancelGeneration -> streamingJob?.cancel()
            is ChatUiEvent.InputChanged -> _uiState.update { it.copy(inputText = event.text) }
        }
    }

    private fun handleSend(text: String) {
        if (text.isBlank() || _uiState.value.isGenerating) return

        // Add user message immediately
        val userMsg = ChatMessage(text = text, isFromUser = true)
        val aiPlaceholder = ChatMessage(text = "", isFromUser = false)
        _uiState.update {
            it.copy(
                messages = it.messages + userMsg + aiPlaceholder,
                inputText = "",
                isGenerating = true
            )
        }

        streamingJob = viewModelScope.launch {
            val context = buildContext()
            sendMessage(text, context).collect { result ->
                when (result) {
                    is Result.Success -> {
                        // Update last message (AI placeholder) with partial text
                        _uiState.update { state ->
                            val msgs = state.messages.toMutableList()
                            msgs[msgs.lastIndex] = aiPlaceholder.copy(text = result.data)
                            state.copy(messages = msgs)
                        }
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(isGenerating = false, error = result.error.message)
                    }
                    Result.Loading -> Unit
                }
            }
            _uiState.update { it.copy(isGenerating = false) }
        }
    }

    private suspend fun buildContext(): ChatContext {
        val recentMoods = moodDao.getRecentMoodsOnce(limit = 7).map { it.score }
        return ChatContext(moodHistory = recentMoods, journalSummary = "")
    }
}
```

## Security Checklist

- [ ] `GEMINI_API_KEY` only in `local.properties` → `BuildConfig.GEMINI_API_KEY`
- [ ] Key never printed via `Log.*` or exposed in any UI text
- [ ] `local.properties` listed in `.gitignore`
- [ ] SAFETY block present in every prompt
- [ ] Streaming job cancellable via `streamingJob?.cancel()`
