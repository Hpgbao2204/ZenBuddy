package com.zenbuddy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zenbuddy.data.local.entity.JournalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalDao {

    @Query("SELECT * FROM journals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllJournals(userId: String): Flow<List<JournalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(journal: JournalEntity)

    @Query("SELECT * FROM journals WHERE isSynced = 0 AND userId = :userId")
    suspend fun getUnsynced(userId: String): List<JournalEntity>

    @Query("UPDATE journals SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>)

    @Query("SELECT * FROM journals WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecent(userId: String, limit: Int = 3): List<JournalEntity>
}
