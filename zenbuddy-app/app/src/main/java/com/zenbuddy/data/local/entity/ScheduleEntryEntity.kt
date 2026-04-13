package com.zenbuddy.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zenbuddy.domain.model.ScheduleEntry
import java.util.UUID

@Entity(tableName = "schedule_entries", indices = [Index("date")])
data class ScheduleEntryEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val type: String,            // "meal", "exercise", "reminder", "custom"
    val date: String,            // "2026-04-13"
    val timeHour: Int,
    val timeMinute: Int,
    val isCompleted: Boolean = false,
    val isReminderEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

fun ScheduleEntryEntity.toDomain(): ScheduleEntry = ScheduleEntry(
    id = id,
    title = title,
    description = description,
    type = type,
    date = date,
    timeHour = timeHour,
    timeMinute = timeMinute,
    isCompleted = isCompleted,
    isReminderEnabled = isReminderEnabled,
    createdAt = createdAt
)

fun ScheduleEntry.toEntity(): ScheduleEntryEntity = ScheduleEntryEntity(
    id = id,
    title = title,
    description = description,
    type = type,
    date = date,
    timeHour = timeHour,
    timeMinute = timeMinute,
    isCompleted = isCompleted,
    isReminderEnabled = isReminderEnabled,
    createdAt = createdAt
)
