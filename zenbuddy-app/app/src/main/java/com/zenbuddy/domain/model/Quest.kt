package com.zenbuddy.domain.model

import java.util.UUID

data class Quest(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false,
    val generatedForDate: String,
    val createdAt: Long = System.currentTimeMillis()
)
