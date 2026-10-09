package com.intellipaat.learndash.di

import android.content.Context
import androidx.room.Room
import com.intellipaat.learndash.data.local.LearnDatabase
import com.intellipaat.learndash.data.remote.Connectivity
import com.intellipaat.learndash.data.remote.ConnectivityGate
import com.intellipaat.learndash.data.remote.CourseApi
import com.intellipaat.learndash.data.remote.CourseRemote
import com.intellipaat.learndash.data.repo.AuthRepositoryImpl
import com.intellipaat.learndash.data.repo.CourseRepositoryImpl
import com.intellipaat.learndash.domain.repo.AuthRepository
import com.intellipaat.learndash.domain.repo.CourseRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** One module is enough at this size — split it when it earns it. */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun database(@ApplicationContext context: Context): LearnDatabase =
        Room.databaseBuilder(context, LearnDatabase::class.java, "learndash.db")
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides fun courseDao(db: LearnDatabase) = db.courses()
    @Provides fun lessonDao(db: LearnDatabase) = db.lessons()

    @Provides @Singleton
    fun remote(api: CourseApi): CourseRemote = api

    @Provides @Singleton
    fun connectivity(gate: ConnectivityGate): Connectivity = gate

    @Provides @Singleton
    fun courses(repo: CourseRepositoryImpl): CourseRepository = repo

    @Provides @Singleton
    fun auth(repo: AuthRepositoryImpl): AuthRepository = repo
}
