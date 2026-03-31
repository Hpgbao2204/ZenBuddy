package com.zenbuddy.domain.model

import java.util.UUID

data class MoodEntry(
    val id: String = UUID.randomUUID().toString(),
    val score: Int,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
