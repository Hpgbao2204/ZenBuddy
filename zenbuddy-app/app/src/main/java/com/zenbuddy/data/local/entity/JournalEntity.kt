package com.zenbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zenbuddy.domain.model.JournalEntry
import java.util.UUID

@Entity(tableName = "journals")
data class JournalEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val text: String,
    val audioPath: String? = null,
    val isSynced: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

fun JournalEntity.toDomain(): JournalEntry = JournalEntry(
    id = id,
    text = text,
    audioPath = audioPath,
    createdAt = createdAt
)

fun JournalEntry.toEntity(isSynced: Boolean = false): JournalEntity = JournalEntity(
    id = id,
    text = text,
    audioPath = audioPath,
    isSynced = isSynced,
    createdAt = createdAt
)
