package com.zenbuddy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zenbuddy.data.local.entity.StepCountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {

    @Query("SELECT * FROM step_counts WHERE date = :date LIMIT 1")
    fun getStepsByDate(date: String): Flow<StepCountEntity?>

    @Query("SELECT * FROM step_counts WHERE date >= :fromDate ORDER BY date ASC")
    fun getStepsFrom(fromDate: String): Flow<List<StepCountEntity>>

    @Query("SELECT * FROM step_counts ORDER BY date DESC LIMIT :limit")
    fun getRecentSteps(limit: Int = 7): Flow<List<StepCountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(stepCount: StepCountEntity)

    @Query("UPDATE step_counts SET steps = :steps, caloriesBurned = :calories, distanceKm = :distance WHERE date = :date")
    suspend fun updateSteps(date: String, steps: Int, calories: Double, distance: Double)
}
