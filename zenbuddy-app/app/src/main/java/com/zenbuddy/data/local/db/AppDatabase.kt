package com.zenbuddy.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zenbuddy.data.local.dao.ChatDao
import com.zenbuddy.data.local.dao.JournalDao
import com.zenbuddy.data.local.dao.MoodDao
import com.zenbuddy.data.local.dao.QuestDao
import com.zenbuddy.data.local.entity.ChatMessageEntity
import com.zenbuddy.data.local.entity.JournalEntity
import com.zenbuddy.data.local.entity.MoodEntity
import com.zenbuddy.data.local.entity.QuestEntity

@Database(
    entities = [
        MoodEntity::class,
        JournalEntity::class,
        ChatMessageEntity::class,
        QuestEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun moodDao(): MoodDao
    abstract fun journalDao(): JournalDao
    abstract fun chatDao(): ChatDao
    abstract fun questDao(): QuestDao
}
