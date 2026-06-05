package com.pedroid.mobyfocus.di

import com.pedroid.mobyfocus.data.usage.UsageStatsDataSource
import com.pedroid.mobyfocus.data.usage.UsageStatsDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataSourceModule {

    @Binds
    abstract fun bindUsageStatsDataSource(impl: UsageStatsDataSourceImpl): UsageStatsDataSource
}
