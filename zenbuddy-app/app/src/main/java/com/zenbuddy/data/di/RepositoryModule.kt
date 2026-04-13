package com.zenbuddy.data.di

import com.zenbuddy.data.repository.AuthRepositoryImpl
import com.zenbuddy.data.repository.ChatRepositoryImpl
import com.zenbuddy.data.repository.ExerciseRepositoryImpl
import com.zenbuddy.data.repository.FoodRepositoryImpl
import com.zenbuddy.data.repository.HealthAiRepositoryImpl
import com.zenbuddy.data.repository.JournalRepositoryImpl
import com.zenbuddy.data.repository.MoodRepositoryImpl
import com.zenbuddy.data.repository.QuestRepositoryImpl
import com.zenbuddy.data.repository.ScheduleRepositoryImpl
import com.zenbuddy.data.repository.StepRepositoryImpl
import com.zenbuddy.data.repository.SyncRepositoryImpl
import com.zenbuddy.data.repository.UserProfileRepositoryImpl
import com.zenbuddy.data.repository.WeatherRepositoryImpl
import com.zenbuddy.domain.repository.AuthRepository
import com.zenbuddy.domain.repository.ChatRepository
import com.zenbuddy.domain.repository.ExerciseRepository
import com.zenbuddy.domain.repository.FoodRepository
import com.zenbuddy.domain.repository.HealthAiRepository
import com.zenbuddy.domain.repository.JournalRepository
import com.zenbuddy.domain.repository.MoodRepository
import com.zenbuddy.domain.repository.QuestRepository
import com.zenbuddy.domain.repository.ScheduleRepository
import com.zenbuddy.domain.repository.StepRepository
import com.zenbuddy.domain.repository.SyncRepository
import com.zenbuddy.domain.repository.UserProfileRepository
import com.zenbuddy.domain.repository.WeatherRepository
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

    @Binds
    @Singleton
    abstract fun bindStepRepository(impl: StepRepositoryImpl): StepRepository

    @Binds
    @Singleton
    abstract fun bindFoodRepository(impl: FoodRepositoryImpl): FoodRepository

    @Binds
    @Singleton
    abstract fun bindExerciseRepository(impl: ExerciseRepositoryImpl): ExerciseRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(impl: UserProfileRepositoryImpl): UserProfileRepository

    @Binds
    @Singleton
    abstract fun bindScheduleRepository(impl: ScheduleRepositoryImpl): ScheduleRepository

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(impl: WeatherRepositoryImpl): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindHealthAiRepository(impl: HealthAiRepositoryImpl): HealthAiRepository
}
