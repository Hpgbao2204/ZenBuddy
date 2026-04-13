package com.zenbuddy.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.zenbuddy.app.BuildConfig
import com.zenbuddy.core.di.IoDispatcher
import com.zenbuddy.core.error.AppError
import com.zenbuddy.core.result.Result
import com.zenbuddy.domain.model.ScheduleEntry
import com.zenbuddy.domain.repository.HealthAiRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.util.UUID
import javax.inject.Inject

class HealthAiRepositoryImpl @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : HealthAiRepository {

    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            temperature = 0.7f
            topP = 0.9f
            maxOutputTokens = 1024
        }
    )

    override fun generateMealPlan(
        tdee: Double,
        goalType: String,
        preferences: String
    ): Flow<Result<String>> = flow {
        emit(Result.Loading)
        val calorieTarget = when (goalType) {
            "lose" -> tdee - 500
            "gain" -> tdee + 300
            else -> tdee
        }
        val prompt = """
            Bạn là chuyên gia dinh dưỡng Việt Nam. Tạo thực đơn 1 ngày bằng tiếng Việt.
            
            Thông tin:
            - TDEE: ${tdee.toInt()} kcal
            - Mục tiêu: $goalType (target: ${calorieTarget.toInt()} kcal/ngày)
            - Sở thích: $preferences
            
            Tạo thực đơn với 3 bữa chính + 2 bữa phụ.
            Mỗi món ghi rõ: tên món, lượng calories ước tính, protein/carb/fat.
            Ưu tiên món ăn Việt Nam phổ biến, dễ nấu.
            Format đẹp, rõ ràng.
        """.trimIndent()

        val responseBuilder = StringBuilder()
        model.generateContentStream(prompt).collect { chunk ->
            val token = chunk.text ?: return@collect
            responseBuilder.append(token)
            emit(Result.Success(responseBuilder.toString()))
        }
    }.catch { e ->
        emit(Result.Error(AppError.AiError(e.message ?: "AI meal plan failed")))
    }.flowOn(ioDispatcher)

    override fun generateWorkoutPlan(
        profile: String,
        goalType: String
    ): Flow<Result<String>> = flow {
        emit(Result.Loading)
        val prompt = """
            Bạn là huấn luyện viên cá nhân chuyên nghiệp. Tạo lịch tập 1 ngày bằng tiếng Việt.
            
            Hồ sơ: $profile
            Mục tiêu: $goalType
            
            Tạo kế hoạch tập luyện chi tiết:
            - Khởi động (5-10 phút)
            - Bài tập chính (30-45 phút) với số hiệp, số lần, thời gian nghỉ
            - Giãn cơ (5-10 phút)
            
            Ghi rõ calories ước tính cho toàn buổi tập.
            Format đẹp, dùng emoji phù hợp.
        """.trimIndent()

        val responseBuilder = StringBuilder()
        model.generateContentStream(prompt).collect { chunk ->
            val token = chunk.text ?: return@collect
            responseBuilder.append(token)
            emit(Result.Success(responseBuilder.toString()))
        }
    }.catch { e ->
        emit(Result.Error(AppError.AiError(e.message ?: "AI workout plan failed")))
    }.flowOn(ioDispatcher)

    override fun generateDailySchedule(
        profileSummary: String,
        currentSchedule: List<ScheduleEntry>
    ): Flow<Result<List<ScheduleEntry>>> = flow {
        emit(Result.Loading)
        val prompt = """
            Bạn là chuyên gia sức khỏe. Tạo lịch trình 1 ngày bằng tiếng Việt.
            
            Hồ sơ: $profileSummary
            Lịch hiện tại: ${if (currentSchedule.isEmpty()) "Trống" else currentSchedule.joinToString(", ") { "${it.timeHour}:${it.timeMinute} - ${it.title}" }}
            
            Trả về JSON array (không markdown) gồm các mục:
            [{"title":"Tên","description":"Mô tả","type":"meal/exercise/reminder","timeHour":7,"timeMinute":0}]
            
            Bao gồm: bữa ăn, tập luyện, uống nước, nghỉ ngơi. Fit với lịch hiện tại.
        """.trimIndent()

        val response = model.generateContent(prompt)
        val text = response.text?.trim() ?: throw Exception("No response")
        val json = text.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()

        val type = object : TypeToken<List<ScheduleItemDto>>() {}.type
        val items: List<ScheduleItemDto> = Gson().fromJson(json, type)

        val entries = items.map { dto ->
            ScheduleEntry(
                id = UUID.randomUUID().toString(),
                title = dto.title,
                description = dto.description,
                type = dto.type,
                timeHour = dto.timeHour,
                timeMinute = dto.timeMinute
            )
        }
        emit(Result.Success(entries))
    }.catch { e ->
        emit(Result.Error(AppError.AiError(e.message ?: "AI schedule failed")))
    }.flowOn(ioDispatcher)

    override fun getHealthAdvice(
        question: String,
        profileSummary: String,
        todaySteps: Int,
        todayCalories: Double
    ): Flow<Result<String>> = flow {
        emit(Result.Loading)
        val prompt = """
            [HỆ THỐNG]
            Bạn là trợ lý sức khỏe AI thông minh, thân thiện. Trả lời bằng tiếng Việt.
            Không chẩn đoán bệnh. Gợi ý lành mạnh, khoa học.
            
            [HỒ SƠ NGƯỜI DÙNG]
            $profileSummary
            
            [DỮ LIỆU HÔM NAY]
            - Bước chân: $todaySteps
            - Calories nạp: ${todayCalories.toInt()} kcal
            
            [CÂU HỎI]
            $question
            
            Trả lời ngắn gọn, thực tế, dễ hiểu. Dùng emoji phù hợp.
        """.trimIndent()

        val responseBuilder = StringBuilder()
        model.generateContentStream(prompt).collect { chunk ->
            val token = chunk.text ?: return@collect
            responseBuilder.append(token)
            emit(Result.Success(responseBuilder.toString()))
        }
    }.catch { e ->
        emit(Result.Error(AppError.AiError(e.message ?: "AI advice failed")))
    }.flowOn(ioDispatcher)
}

private data class ScheduleItemDto(
    val title: String = "",
    val description: String = "",
    val type: String = "custom",
    val timeHour: Int = 0,
    val timeMinute: Int = 0
)
