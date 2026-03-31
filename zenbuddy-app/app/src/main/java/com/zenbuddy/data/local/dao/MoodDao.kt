package com.zenbuddy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zenbuddy.data.local.entity.MoodEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {

    @Query("SELECT * FROM moods ORDER BY createdAt DESC")
    fun getAllMoods(): Flow<List<MoodEntity>>

    @Query("SELECT * FROM moods WHERE createdAt >= :from ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentMoods(from: Long, limit: Int = 7): Flow<List<MoodEntity>>

    @Query("SELECT * FROM moods WHERE createdAt >= :startOfDay ORDER BY createdAt DESC LIMIT 1")
    fun getTodayMood(startOfDay: Long): Flow<MoodEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mood: MoodEntity)

    @Update
    suspend fun update(mood: MoodEntity)

    @Query("SELECT * FROM moods WHERE isSynced = 0")
    suspend fun getUnsynced(): List<MoodEntity>

    @Query("UPDATE moods SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>)

    @Query("SELECT * FROM moods WHERE createdAt >= :fromTimestamp ORDER BY createdAt ASC")
    fun getMoodsFrom(fromTimestamp: Long): Flow<List<MoodEntity>>
}
