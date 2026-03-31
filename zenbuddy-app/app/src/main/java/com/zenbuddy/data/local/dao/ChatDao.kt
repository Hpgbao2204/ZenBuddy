package com.zenbuddy.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.zenbuddy.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Query("SELECT * FROM chat_messages WHERE userId = :userId AND sessionId = :sessionId ORDER BY createdAt ASC")
    fun getMessagesBySession(userId: String, sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE userId = :userId ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentMessages(userId: String, limit: Int = 50): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId AND userId = :userId")
    suspend fun deleteSession(userId: String, sessionId: String)
}
