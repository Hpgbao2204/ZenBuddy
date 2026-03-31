package com.zenbuddy.data.repository

import com.zenbuddy.app.BuildConfig
import com.zenbuddy.core.di.IoDispatcher
import com.zenbuddy.core.error.AppError
import com.zenbuddy.core.result.Result
import com.zenbuddy.data.local.dao.ChatDao
import com.zenbuddy.data.local.entity.ChatMessageEntity
import com.zenbuddy.data.local.entity.toDomain
import com.zenbuddy.data.local.entity.toEntity
import com.zenbuddy.domain.model.ChatContext
import com.zenbuddy.domain.model.ChatMessage
import com.zenbuddy.domain.repository.AuthRepository
import com.zenbuddy.domain.repository.ChatRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatDao: ChatDao,
    private val authRepository: AuthRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ChatRepository {

    companion object {
        private fun friendlyAiError(e: Throwable): String {
            val msg = e.message ?: "Unknown AI error"
            return when {
                msg.contains("quota", ignoreCase = true) || msg.contains("rate", ignoreCase = true) ->
                    "AI is taking a break — free tier limit reached. Please try again later 🌙"
                msg.contains("network", ignoreCase = true) || msg.contains("connect", ignoreCase = true) ->
                    "No internet connection. Please check your network 📡"
                msg.contains("api key", ignoreCase = true) || msg.contains("apikey", ignoreCase = true) ||
                msg.contains("API_KEY_INVALID", ignoreCase = true) ->
                    "Invalid API key. Please check your Gemini key in settings 🔑"
                msg.contains("not found", ignoreCase = true) || msg.contains("404", ignoreCase = true) ->
                    "AI model not available. Please try again later 🔄"
                else -> "AI error: ${msg.take(100)} 💜"
            }
        }
    }

    private fun uid(): String = authRepository.getCurrentUserId() ?: ""

    private val model = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f
            topP = 0.9f
            maxOutputTokens = 1024
        }
    )

    private val shortModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.8f
            topP = 0.9f
            maxOutputTokens = 256
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
            emit(Result.Success(responseBuilder.toString()))
        }
    }.catch { e ->
        emit(Result.Error(AppError.AiError(friendlyAiError(e))))
    }.flowOn(ioDispatcher)

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
        emit(Result.Error(AppError.AiError(friendlyAiError(e))))
    }.flowOn(ioDispatcher)

    override fun getMessages(sessionId: String): Flow<Result<List<ChatMessage>>> =
        chatDao.getMessagesBySession(uid(), sessionId)
            .map<_, Result<List<ChatMessage>>> { entities ->
                Result.Success(entities.map { it.toDomain() })
            }
            .catch { emit(Result.Error(AppError.DatabaseError(it.message ?: "DB error"))) }
            .flowOn(ioDispatcher)

    override suspend fun saveMessage(message: ChatMessage): Result<Unit> =
        runCatching { chatDao.insert(message.toEntity(userId = uid())) }
            .fold(
                onSuccess = { Result.Success(Unit) },
                onFailure = { Result.Error(AppError.DatabaseError(it.message ?: "Save failed")) }
            )

    private fun buildPrompt(userMessage: String, context: ChatContext): String = """
        [SYSTEM_ROLE]
        You are ZenBuddy, an empathetic non-prescriptive mental health companion.
        Never diagnose or prescribe. Suggest only gentle micro-actions.
        Keep responses warm, concise, and use occasional emoji 💜

        [SAFETY]
        If the user mentions self-harm, suicide, or hurting themselves:
        - Express care immediately
        - Provide crisis hotline: 988 (US), 1800-599-0019 (VN), or local emergency
        - Do NOT continue casual conversation

        [USER_CONTEXT]
        Recent mood scores (last 7 days): ${context.moodHistory.joinToString(", ")}
        Recent journals: ${context.journalSummary}

        [GOAL]
        Respond to: "$userMessage"
        Be empathetic first. Then suggest 1 gentle micro-task. End with a warm check-in question.
    """.trimIndent()

    override suspend fun generateAffirmation(moodScore: Int, recentJournal: String): Result<String> =
        runCatching {
            val prompt = "Generate one short positive affirmation (1-2 sentences) for someone with mood $moodScore/10. Be warm and encouraging. End with an emoji. No prefix."
            val response = shortModel.generateContent(prompt)
            response.text?.trim() ?: "You are worthy of peace and joy 💜"
        }.fold(
            onSuccess = { Result.Success(it) },
            onFailure = { Result.Error(AppError.AiError(friendlyAiError(it))) }
        )

    override suspend fun generateMoodInsight(moodScores: List<Int>, days: Int): Result<String> =
        runCatching {
            val prompt = """
                You are ZenBuddy, a caring mental health companion 🧠💜
                Analyze these mood scores from the last $days days: ${moodScores.joinToString(", ")}
                (Scale: 0=very sad, 10=very happy)
                
                Provide a SHORT, warm insight (3-4 sentences max):
                1. Identify any pattern or trend
                2. Acknowledge their feelings
                3. Offer one gentle suggestion
                
                Keep the tone supportive, not clinical. Use emoji sparingly.
                If there's not enough data, encourage them to keep tracking.
            """.trimIndent()
            val response = model.generateContent(prompt)
            response.text?.trim() ?: "Keep tracking your mood — patterns will emerge soon! 🌱"
        }.fold(
            onSuccess = { Result.Success(it) },
            onFailure = { Result.Error(AppError.AiError(friendlyAiError(it))) }
        )

    override suspend fun generateJournalReflection(journalText: String): Result<String> =
        runCatching {
            val prompt = "Reflect briefly (2-3 sentences) on this journal entry: \"${journalText.take(300)}\". Acknowledge their feelings and ask one follow-up question. Be warm."
            val response = shortModel.generateContent(prompt)
            response.text?.trim() ?: "Thank you for sharing. What's the strongest emotion you feel right now? 💜"
        }.fold(
            onSuccess = { Result.Success(it) },
            onFailure = { Result.Error(AppError.AiError(friendlyAiError(it))) }
        )

    private fun parseTasksJson(raw: String): List<String> {
        // Extract strings between quotes from JSON array like ["task1", "task2", "task3"]
        val pattern = Regex(""""([^"]+)"""")
        val matches = pattern.findAll(raw).map { it.groupValues[1] }.toList()
        return if (matches.isNotEmpty()) matches else {
            // Fallback: split by newlines for numbered list responses like "1. Task one\n2. Task two"
            raw.lines()
                .map { it.trim().removePrefix("-").removePrefix("1.").removePrefix("2.").removePrefix("3.").trim() }
                .filter { it.isNotBlank() && it.length > 3 }
                .take(3)
        }
    }
}
