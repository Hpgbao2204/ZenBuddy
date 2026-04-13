package com.zenbuddy.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zenbuddy.data.local.entity.FoodEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    @Query("SELECT * FROM food_entries WHERE date = :date ORDER BY createdAt ASC")
    fun getFoodByDate(date: String): Flow<List<FoodEntryEntity>>

    @Query("SELECT SUM(calories) FROM food_entries WHERE date = :date")
    fun getTotalCaloriesByDate(date: String): Flow<Double?>

    @Query("SELECT * FROM food_entries ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentFood(limit: Int = 20): Flow<List<FoodEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: FoodEntryEntity)

    @Delete
    suspend fun delete(entry: FoodEntryEntity)

    @Query("DELETE FROM food_entries WHERE id = :id")
    suspend fun deleteById(id: String)
}
