package com.zenbuddy.data.repository

import com.zenbuddy.core.di.IoDispatcher
import com.zenbuddy.core.error.AppError
import com.zenbuddy.core.result.Result
import com.zenbuddy.data.local.dao.ExerciseDao
import com.zenbuddy.data.local.entity.ExerciseEntity
import com.zenbuddy.data.local.entity.toDomain
import com.zenbuddy.domain.model.Exercise
import com.zenbuddy.domain.repository.ExerciseRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExerciseRepositoryImpl @Inject constructor(
    private val exerciseDao: ExerciseDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ExerciseRepository {

    override fun getAllExercises(): Flow<Result<List<Exercise>>> =
        exerciseDao.getAllExercises()
            .map { entities -> Result.Success(entities.map { it.toDomain() }) }
            .catch { emit(Result.Error(AppError.DatabaseError(it.message ?: "DB error"))) }
            .flowOn(ioDispatcher)

    override fun getByMuscleGroup(group: String): Flow<Result<List<Exercise>>> =
        exerciseDao.getByMuscleGroup(group)
            .map { entities -> Result.Success(entities.map { it.toDomain() }) }
            .catch { emit(Result.Error(AppError.DatabaseError(it.message ?: "DB error"))) }
            .flowOn(ioDispatcher)

    override fun getMuscleGroups(): Flow<Result<List<String>>> =
        exerciseDao.getMuscleGroups()
            .map { Result.Success(it) }
            .catch { emit(Result.Error(AppError.DatabaseError(it.message ?: "DB error"))) }
            .flowOn(ioDispatcher)

    override suspend fun getById(id: String): Result<Exercise> =
        withContext(ioDispatcher) {
            runCatching { exerciseDao.getById(id)?.toDomain() }.fold(
                onSuccess = { exercise ->
                    if (exercise != null) Result.Success(exercise)
                    else Result.Error(AppError.DatabaseError("Exercise not found"))
                },
                onFailure = { Result.Error(AppError.DatabaseError(it.message ?: "Query failed")) }
            )
        }

    override suspend fun seedIfEmpty() = withContext(ioDispatcher) {
        if (exerciseDao.count() == 0) {
            exerciseDao.insertAll(defaultExercises)
        }
    }

    companion object {
        val defaultExercises = listOf(
            // Chest
            ExerciseEntity(id = "ex_1", name = "Hít đất (Push-ups)", description = "Bài tập cơ bản cho ngực và tay", muscleGroup = "chest", difficulty = "beginner", durationMinutes = 10, caloriesPerMinute = 7.0, instructions = "1. Nằm sấp, tay chống rộng bằng vai\n2. Đẩy người lên bằng tay\n3. Hạ người xuống từ từ\n4. Lặp lại 10-15 lần x 3 hiệp"),
            ExerciseEntity(id = "ex_2", name = "Nằm đẩy tạ (Bench Press)", description = "Bài tập tạ cho cơ ngực", muscleGroup = "chest", difficulty = "intermediate", durationMinutes = 15, caloriesPerMinute = 8.0, instructions = "1. Nằm trên ghế phẳng\n2. Cầm tạ rộng hơn vai\n3. Hạ tạ xuống ngực\n4. Đẩy lên hoàn toàn\n5. 8-12 lần x 4 hiệp"),
            ExerciseEntity(id = "ex_3", name = "Bay tạ (Dumbbell Fly)", description = "Tạo hình cơ ngực", muscleGroup = "chest", difficulty = "intermediate", durationMinutes = 12, caloriesPerMinute = 6.0, instructions = "1. Nằm trên ghế, cầm 2 tạ đơn\n2. Mở rộng tay sang bên\n3. Khép tay lại phía trước\n4. 10-12 lần x 3 hiệp"),
            // Back
            ExerciseEntity(id = "ex_4", name = "Kéo xà (Pull-ups)", description = "Bài tập cơ lưng bằng xà đơn", muscleGroup = "back", difficulty = "intermediate", durationMinutes = 10, caloriesPerMinute = 8.0, instructions = "1. Treo người trên xà\n2. Kéo người lên đến cằm qua xà\n3. Hạ từ từ\n4. 5-10 lần x 3 hiệp"),
            ExerciseEntity(id = "ex_5", name = "Chèo tạ (Bent-over Row)", description = "Tập cơ lưng giữa", muscleGroup = "back", difficulty = "intermediate", durationMinutes = 12, caloriesPerMinute = 7.0, instructions = "1. Cúi người 45 độ\n2. Kéo tạ lên bụng\n3. Hạ từ từ\n4. 10-12 lần x 3 hiệp"),
            ExerciseEntity(id = "ex_6", name = "Superman", description = "Tăng cường cơ lưng dưới", muscleGroup = "back", difficulty = "beginner", durationMinutes = 8, caloriesPerMinute = 5.0, instructions = "1. Nằm sấp trên sàn\n2. Nâng đồng thời tay và chân\n3. Giữ 2-3 giây\n4. Hạ xuống\n5. 12-15 lần x 3 hiệp"),
            // Legs
            ExerciseEntity(id = "ex_7", name = "Squat", description = "Bài tập chân cơ bản", muscleGroup = "legs", difficulty = "beginner", durationMinutes = 10, caloriesPerMinute = 8.0, instructions = "1. Đứng rộng bằng vai\n2. Hạ thấp như ngồi ghế\n3. Đùi song song mặt đất\n4. Đứng lên\n5. 15-20 lần x 3 hiệp"),
            ExerciseEntity(id = "ex_8", name = "Lunges", description = "Bài tập chân một bên", muscleGroup = "legs", difficulty = "beginner", durationMinutes = 10, caloriesPerMinute = 7.0, instructions = "1. Bước dài một chân về trước\n2. Hạ gối sau gần sàn\n3. Đẩy lên về vị trí ban đầu\n4. Đổi chân\n5. 10 lần mỗi chân x 3 hiệp"),
            ExerciseEntity(id = "ex_9", name = "Deadlift", description = "Bài tập tổng hợp mạnh nhất", muscleGroup = "legs", difficulty = "advanced", durationMinutes = 15, caloriesPerMinute = 9.0, instructions = "1. Đứng gần thanh tạ\n2. Cúi xuống nắm tạ\n3. Đẩy hông về trước, nâng tạ\n4. Giữ lưng thẳng\n5. 5-8 lần x 4 hiệp"),
            // Arms
            ExerciseEntity(id = "ex_10", name = "Cuốn tạ (Bicep Curl)", description = "Tập cơ bắp tay trước", muscleGroup = "arms", difficulty = "beginner", durationMinutes = 10, caloriesPerMinute = 5.0, instructions = "1. Đứng thẳng, cầm tạ\n2. Gập khuỷu tay đưa tạ lên\n3. Hạ từ từ\n4. 12-15 lần x 3 hiệp"),
            ExerciseEntity(id = "ex_11", name = "Hít đất kim cương", description = "Tập cơ tam đầu", muscleGroup = "arms", difficulty = "intermediate", durationMinutes = 10, caloriesPerMinute = 7.0, instructions = "1. Vị trí hít đất, hai bàn tay gần nhau\n2. Ngón cái và ngón trỏ tạo hình kim cương\n3. Hạ người xuống\n4. Đẩy lên\n5. 8-12 lần x 3 hiệp"),
            // Shoulders
            ExerciseEntity(id = "ex_12", name = "Ép vai (Shoulder Press)", description = "Tập cơ vai", muscleGroup = "shoulders", difficulty = "intermediate", durationMinutes = 12, caloriesPerMinute = 6.0, instructions = "1. Ngồi hoặc đứng, cầm tạ ngang vai\n2. Đẩy tạ lên qua đầu\n3. Hạ từ từ về vai\n4. 10-12 lần x 3 hiệp"),
            ExerciseEntity(id = "ex_13", name = "Nâng tạ ngang (Lateral Raise)", description = "Tập cơ vai giữa", muscleGroup = "shoulders", difficulty = "beginner", durationMinutes = 10, caloriesPerMinute = 5.0, instructions = "1. Đứng thẳng, cầm tạ hai bên\n2. Nâng tạ sang ngang lên ngang vai\n3. Hạ từ từ\n4. 12-15 lần x 3 hiệp"),
            // Core
            ExerciseEntity(id = "ex_14", name = "Plank", description = "Tập cơ cốt lõi", muscleGroup = "core", difficulty = "beginner", durationMinutes = 5, caloriesPerMinute = 5.0, instructions = "1. Chống khuỷu tay, thẳng người\n2. Giữ thân thẳng\n3. Giữ 30-60 giây x 3 hiệp"),
            ExerciseEntity(id = "ex_15", name = "Gập bụng (Crunches)", description = "Tập cơ bụng trên", muscleGroup = "core", difficulty = "beginner", durationMinutes = 8, caloriesPerMinute = 6.0, instructions = "1. Nằm ngửa, gập gối\n2. Nâng vai lên khỏi sàn\n3. Siết cơ bụng\n4. Hạ từ từ\n5. 15-20 lần x 3 hiệp"),
            ExerciseEntity(id = "ex_16", name = "Russian Twist", description = "Tập cơ bụng chéo", muscleGroup = "core", difficulty = "intermediate", durationMinutes = 8, caloriesPerMinute = 7.0, instructions = "1. Ngồi, nghiêng lưng 45 độ\n2. Nâng chân khỏi sàn\n3. Xoay thân sang trái-phải\n4. 20 lần x 3 hiệp"),
            // Cardio
            ExerciseEntity(id = "ex_17", name = "Chạy bộ tại chỗ", description = "Cardio cơ bản", muscleGroup = "cardio", difficulty = "beginner", durationMinutes = 15, caloriesPerMinute = 10.0, instructions = "1. Đứng tại chỗ\n2. Chạy nâng cao gối\n3. Duy trì nhịp đều\n4. 15-30 phút"),
            ExerciseEntity(id = "ex_18", name = "Burpees", description = "Bài tập toàn thân cường độ cao", muscleGroup = "cardio", difficulty = "advanced", durationMinutes = 10, caloriesPerMinute = 12.0, instructions = "1. Đứng thẳng\n2. Squat xuống, tay chống sàn\n3. Nhảy chân ra sau (tư thế plank)\n4. Hít đất 1 lần\n5. Nhảy chân về\n6. Nhảy lên cao\n7. 10-15 lần x 3 hiệp"),
            ExerciseEntity(id = "ex_19", name = "Nhảy dây", description = "Cardio hiệu quả cao", muscleGroup = "cardio", difficulty = "beginner", durationMinutes = 15, caloriesPerMinute = 11.0, instructions = "1. Cầm dây, đứng thẳng\n2. Nhảy đều đặn\n3. Duy trì nhịp thở\n4. 15-30 phút"),
            ExerciseEntity(id = "ex_20", name = "Mountain Climbers", description = "Cardio + cơ bụng", muscleGroup = "cardio", difficulty = "intermediate", durationMinutes = 10, caloriesPerMinute = 11.0, instructions = "1. Tư thế plank cao\n2. Đưa gối lên ngực luân phiên\n3. Tốc độ nhanh\n4. 30 giây x 5 hiệp")
        )
    }
}
