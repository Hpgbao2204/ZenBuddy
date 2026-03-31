package com.zenbuddy.domain.model

import java.util.UUID

data class JournalEntry(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val audioPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
