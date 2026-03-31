package com.zenbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zenbuddy.domain.model.Quest
import java.util.UUID

@Entity(tableName = "quests")
data class QuestEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false,
    val generatedForDate: String,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

fun QuestEntity.toDomain(): Quest = Quest(
    id = id,
    title = title,
    isCompleted = isCompleted,
    generatedForDate = generatedForDate,
    createdAt = createdAt
)

fun Quest.toEntity(isSynced: Boolean = false): QuestEntity = QuestEntity(
    id = id,
    title = title,
    isCompleted = isCompleted,
    generatedForDate = generatedForDate,
    isSynced = isSynced,
    createdAt = createdAt
)
