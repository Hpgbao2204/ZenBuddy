package com.zenbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zenbuddy.domain.model.MoodEntry
import java.util.UUID

@Entity(tableName = "moods", indices = [Index("createdAt")])
data class MoodEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
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

fun MoodEntry.toEntity(isSynced: Boolean = false): MoodEntity = MoodEntity(
    id = id,
    score = score,
    note = note,
    isSynced = isSynced,
    createdAt = createdAt
)
