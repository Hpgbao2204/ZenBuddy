package com.zenbuddy.domain.model

data class ScheduleEntry(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "custom",
    val date: String = "",
    val timeHour: Int = 0,
    val timeMinute: Int = 0,
    val isCompleted: Boolean = false,
    val isReminderEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
