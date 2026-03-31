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
            emit(Result.Success(responseBuilder.toString()))
        }
    }.catch { e ->
        emit(Result.Error(AppError.AiError(e.message ?: "Gemini error")))
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
        emit(Result.Error(AppError.AiError(e.message ?: "Quest generation failed")))
    }.flowOn(ioDispatcher)

    override fun getMessages(sessionId: String): Flow<Result<List<ChatMessage>>> =
        chatDao.getMessagesBySession(sessionId)
            .map<_, Result<List<ChatMessage>>> { entities ->
                Result.Success(entities.map { it.toDomain() })
            }
            .catch { emit(Result.Error(AppError.DatabaseError(it.message ?: "DB error"))) }
            .flowOn(ioDispatcher)

    override suspend fun saveMessage(message: ChatMessage): Result<Unit> =
        runCatching { chatDao.insert(message.toEntity()) }
            .fold(
                onSuccess = { Result.Success(Unit) },
                onFailure = { Result.Error(AppError.DatabaseError(it.message ?: "Save failed")) }
            )

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

    private fun parseTasksJson(raw: String): List<String> {
        val cleaned = raw.substringAfter("[").substringBefore("]")
        return cleaned.split(",").map { it.trim().removeSurrounding("\"") }
    }
}
