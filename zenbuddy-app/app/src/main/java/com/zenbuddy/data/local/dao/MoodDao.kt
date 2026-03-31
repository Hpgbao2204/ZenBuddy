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

    @Query("SELECT * FROM moods WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllMoods(userId: String): Flow<List<MoodEntity>>

    @Query("SELECT * FROM moods WHERE userId = :userId AND createdAt >= :from ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentMoods(userId: String, from: Long, limit: Int = 7): Flow<List<MoodEntity>>

    @Query("SELECT * FROM moods WHERE userId = :userId AND createdAt >= :startOfDay ORDER BY createdAt DESC LIMIT 1")
    fun getTodayMood(userId: String, startOfDay: Long): Flow<MoodEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(mood: MoodEntity)

    @Update
    suspend fun update(mood: MoodEntity)

    @Query("SELECT * FROM moods WHERE isSynced = 0 AND userId = :userId")
    suspend fun getUnsynced(userId: String): List<MoodEntity>

    @Query("UPDATE moods SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>)

    @Query("SELECT * FROM moods WHERE userId = :userId AND createdAt >= :fromTimestamp ORDER BY createdAt ASC")
    fun getMoodsFrom(userId: String, fromTimestamp: Long): Flow<List<MoodEntity>>
}
