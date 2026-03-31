package com.zenbuddy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zenbuddy.data.local.entity.QuestEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {

    @Query("SELECT * FROM quests WHERE generatedForDate = :date ORDER BY createdAt ASC")
    fun getQuestsForDate(date: String): Flow<List<QuestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(quests: List<QuestEntity>)

    @Update
    suspend fun update(quest: QuestEntity)

    @Query("SELECT * FROM quests WHERE isSynced = 0")
    suspend fun getUnsynced(): List<QuestEntity>

    @Query("UPDATE quests SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>)

    @Query("SELECT COUNT(*) FROM quests WHERE generatedForDate = :date AND isCompleted = 0")
    fun getActiveQuestCount(date: String): Flow<Int>

    @Query("SELECT * FROM quests WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): QuestEntity?
}
