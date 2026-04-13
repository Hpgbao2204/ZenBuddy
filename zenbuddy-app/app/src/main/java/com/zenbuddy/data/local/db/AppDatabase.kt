package com.zenbuddy.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.zenbuddy.data.local.dao.ChatDao
import com.zenbuddy.data.local.dao.ExerciseDao
import com.zenbuddy.data.local.dao.FoodDao
import com.zenbuddy.data.local.dao.JournalDao
import com.zenbuddy.data.local.dao.MoodDao
import com.zenbuddy.data.local.dao.QuestDao
import com.zenbuddy.data.local.dao.ScheduleDao
import com.zenbuddy.data.local.dao.StepDao
import com.zenbuddy.data.local.dao.UserProfileDao
import com.zenbuddy.data.local.entity.ChatMessageEntity
import com.zenbuddy.data.local.entity.ExerciseEntity
import com.zenbuddy.data.local.entity.FoodEntryEntity
import com.zenbuddy.data.local.entity.JournalEntity
import com.zenbuddy.data.local.entity.MoodEntity
import com.zenbuddy.data.local.entity.QuestEntity
import com.zenbuddy.data.local.entity.ScheduleEntryEntity
import com.zenbuddy.data.local.entity.StepCountEntity
import com.zenbuddy.data.local.entity.UserProfileEntity

@Database(
    entities = [
        MoodEntity::class,
        JournalEntity::class,
        ChatMessageEntity::class,
        QuestEntity::class,
        StepCountEntity::class,
        FoodEntryEntity::class,
        ExerciseEntity::class,
        UserProfileEntity::class,
        ScheduleEntryEntity::class
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun moodDao(): MoodDao
    abstract fun journalDao(): JournalDao
    abstract fun chatDao(): ChatDao
    abstract fun questDao(): QuestDao
    abstract fun stepDao(): StepDao
    abstract fun foodDao(): FoodDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun scheduleDao(): ScheduleDao
}
