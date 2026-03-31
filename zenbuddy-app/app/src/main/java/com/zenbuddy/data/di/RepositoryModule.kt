package com.zenbuddy.data.di

import com.zenbuddy.data.repository.AuthRepositoryImpl
import com.zenbuddy.data.repository.ChatRepositoryImpl
import com.zenbuddy.data.repository.JournalRepositoryImpl
import com.zenbuddy.data.repository.MoodRepositoryImpl
import com.zenbuddy.data.repository.QuestRepositoryImpl
import com.zenbuddy.data.repository.SyncRepositoryImpl
import com.zenbuddy.domain.repository.AuthRepository
import com.zenbuddy.domain.repository.ChatRepository
import com.zenbuddy.domain.repository.JournalRepository
import com.zenbuddy.domain.repository.MoodRepository
import com.zenbuddy.domain.repository.QuestRepository
import com.zenbuddy.domain.repository.SyncRepository
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

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSyncRepository(impl: SyncRepositoryImpl): SyncRepository
}
