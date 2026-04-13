package com.zenbuddy.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.zenbuddy.app.BuildConfig
import com.zenbuddy.core.di.IoDispatcher
import com.zenbuddy.core.error.AppError
import com.zenbuddy.core.result.Result
import com.zenbuddy.data.local.dao.FoodDao
import com.zenbuddy.data.local.entity.FoodEntryEntity
import com.zenbuddy.data.local.entity.toDomain
import com.zenbuddy.data.local.entity.toEntity
import com.zenbuddy.domain.model.FoodEntry
import com.zenbuddy.domain.repository.FoodRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject

class FoodRepositoryImpl @Inject constructor(
    private val foodDao: FoodDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : FoodRepository {

    private val visionModel = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.3f
            maxOutputTokens = 512
        }
    )

    override fun getFoodByDate(date: String): Flow<Result<List<FoodEntry>>> =
        foodDao.getFoodByDate(date)
            .map<_, Result<List<FoodEntry>>> { entities -> Result.Success(entities.map { it.toDomain() }) }
            .catch { emit(Result.Error(AppError.DatabaseError(it.message ?: "DB error"))) }
            .flowOn(ioDispatcher)

    override fun getTotalCaloriesByDate(date: String): Flow<Result<Double>> =
        foodDao.getTotalCaloriesByDate(date)
            .map<_, Result<Double>> { Result.Success(it ?: 0.0) }
            .catch { emit(Result.Error(AppError.DatabaseError(it.message ?: "DB error"))) }
            .flowOn(ioDispatcher)

    override suspend fun addFood(entry: FoodEntry): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching { foodDao.insert(entry.toEntity()) }.fold(
                onSuccess = { Result.Success(Unit) },
                onFailure = { Result.Error(AppError.DatabaseError(it.message ?: "Insert failed")) }
            )
        }

    override suspend fun deleteFood(id: String): Result<Unit> =
        withContext(ioDispatcher) {
            runCatching { foodDao.deleteById(id) }.fold(
                onSuccess = { Result.Success(Unit) },
                onFailure = { Result.Error(AppError.DatabaseError(it.message ?: "Delete failed")) }
            )
        }

    override fun analyzeFoodImage(imageBytes: ByteArray): Flow<Result<FoodEntry>> = flow {
        emit(Result.Loading)
        val prompt = """
            Analyze this food image. Respond in exactly this JSON format (no markdown):
            {"name":"food name","calories":123,"protein":10.0,"carbs":20.0,"fat":5.0}
            Estimate nutrition per serving. Be accurate for Vietnamese cuisine.
        """.trimIndent()

        val response = visionModel.generateContent(
            content {
                image(android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size))
                text(prompt)
            }
        )
        val text = response.text?.trim() ?: throw Exception("No response")
        val json = text.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()

        val gson = com.google.gson.Gson()
        val parsed = gson.fromJson(json, FoodNutrition::class.java)
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        emit(
            Result.Success(
                FoodEntry(
                    id = UUID.randomUUID().toString(),
                    name = parsed.name,
                    calories = parsed.calories,
                    protein = parsed.protein,
                    carbs = parsed.carbs,
                    fat = parsed.fat,
                    mealType = "snack",
                    date = today
                )
            )
        )
    }.catch { e ->
        emit(Result.Error(AppError.AiError(e.message ?: "Food analysis failed")))
    }.flowOn(ioDispatcher)
}

private data class FoodNutrition(
    val name: String = "",
    val calories: Double = 0.0,
    val protein: Double = 0.0,
    val carbs: Double = 0.0,
    val fat: Double = 0.0
)
