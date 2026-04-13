package com.zenbuddy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zenbuddy.data.local.entity.ExerciseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {

    @Query("SELECT * FROM exercises ORDER BY muscleGroup, name")
    fun getAllExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE muscleGroup = :group ORDER BY name")
    fun getByMuscleGroup(group: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE difficulty = :difficulty ORDER BY name")
    fun getByDifficulty(difficulty: String): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): ExerciseEntity?

    @Query("SELECT DISTINCT muscleGroup FROM exercises ORDER BY muscleGroup")
    fun getMuscleGroups(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(exercises: List<ExerciseEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exercise: ExerciseEntity)

    @Query("SELECT COUNT(*) FROM exercises")
    suspend fun count(): Int
}
