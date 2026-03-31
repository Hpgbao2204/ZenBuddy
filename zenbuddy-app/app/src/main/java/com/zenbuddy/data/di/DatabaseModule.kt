package com.zenbuddy.data.di

import android.content.Context
import androidx.room.Room
import com.zenbuddy.data.local.dao.ChatDao
import com.zenbuddy.data.local.dao.JournalDao
import com.zenbuddy.data.local.dao.MoodDao
import com.zenbuddy.data.local.dao.QuestDao
import com.zenbuddy.data.local.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "zenbuddy.db"
        ).build()

    @Provides
    fun provideMoodDao(db: AppDatabase): MoodDao = db.moodDao()

    @Provides
    fun provideJournalDao(db: AppDatabase): JournalDao = db.journalDao()

    @Provides
    fun provideChatDao(db: AppDatabase): ChatDao = db.chatDao()

    @Provides
    fun provideQuestDao(db: AppDatabase): QuestDao = db.questDao()
}
