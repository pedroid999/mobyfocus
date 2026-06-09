package com.pedroid.mobyfocus.di

import android.content.Context
import androidx.room.Room
import com.pedroid.mobyfocus.data.local.MobyFocusDatabase
import com.pedroid.mobyfocus.data.local.dao.AppClassificationDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CurrentTimeMillis

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val DATABASE_NAME = "mobyfocus.db"

    @Provides
    @Singleton
    fun provideMobyFocusDatabase(@ApplicationContext context: Context): MobyFocusDatabase =
        Room.databaseBuilder(context, MobyFocusDatabase::class.java, DATABASE_NAME).build()

    @Provides
    fun provideAppClassificationDao(database: MobyFocusDatabase): AppClassificationDao =
        database.appClassificationDao()

    @Provides
    @CurrentTimeMillis
    fun provideCurrentTimeMillis(): () -> Long = System::currentTimeMillis
}
