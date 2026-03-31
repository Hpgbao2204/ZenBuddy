package com.zenbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zenbuddy.domain.model.MoodEntry
import java.util.UUID

@Entity(tableName = "moods", indices = [Index("createdAt"), Index("userId")])
data class MoodEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userId: String = "",
    val score: Int,
    val note: String? = null,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

fun MoodEntity.toDomain(): MoodEntry = MoodEntry(
    id = id,
    score = score,
    note = note,
    createdAt = createdAt
)

fun MoodEntry.toEntity(userId: String = "", isSynced: Boolean = false): MoodEntity = MoodEntity(
    id = id,
    userId = userId,
    score = score,
    note = note,
    isSynced = isSynced,
    createdAt = createdAt
)
