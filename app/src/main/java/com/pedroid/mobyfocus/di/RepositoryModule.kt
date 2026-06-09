package com.pedroid.mobyfocus.di

import com.pedroid.mobyfocus.data.repository.AppClassificationRepositoryImpl
import com.pedroid.mobyfocus.data.repository.AppUsageRepositoryImpl
import com.pedroid.mobyfocus.data.repository.UsageAccessRepositoryImpl
import com.pedroid.mobyfocus.domain.repository.AppClassificationRepository
import com.pedroid.mobyfocus.domain.repository.AppUsageRepository
import com.pedroid.mobyfocus.domain.repository.UsageAccessRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindUsageAccessRepository(impl: UsageAccessRepositoryImpl): UsageAccessRepository

    @Binds
    abstract fun bindAppUsageRepository(impl: AppUsageRepositoryImpl): AppUsageRepository

    @Binds
    abstract fun bindAppClassificationRepository(
        impl: AppClassificationRepositoryImpl,
    ): AppClassificationRepository
}
