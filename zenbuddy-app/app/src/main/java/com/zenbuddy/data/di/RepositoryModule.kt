package com.zenbuddy.data.di

import com.zenbuddy.data.repository.ChatRepositoryImpl
import com.zenbuddy.data.repository.JournalRepositoryImpl
import com.zenbuddy.data.repository.MoodRepositoryImpl
import com.zenbuddy.data.repository.QuestRepositoryImpl
import com.zenbuddy.domain.repository.ChatRepository
import com.zenbuddy.domain.repository.JournalRepository
import com.zenbuddy.domain.repository.MoodRepository
import com.zenbuddy.domain.repository.QuestRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMoodRepository(impl: MoodRepositoryImpl): MoodRepository

    @Binds
    @Singleton
    abstract fun bindJournalRepository(impl: JournalRepositoryImpl): JournalRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(impl: ChatRepositoryImpl): ChatRepository

    @Binds
    @Singleton
    abstract fun bindQuestRepository(impl: QuestRepositoryImpl): QuestRepository
}
