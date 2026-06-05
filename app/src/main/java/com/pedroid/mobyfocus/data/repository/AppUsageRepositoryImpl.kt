package com.pedroid.mobyfocus.data.repository

import com.pedroid.mobyfocus.data.mapper.AppUsageMapper
import com.pedroid.mobyfocus.data.usage.UsageStatsDataSource
import com.pedroid.mobyfocus.di.IoDispatcher
import com.pedroid.mobyfocus.domain.model.AppUsage
import com.pedroid.mobyfocus.domain.repository.AppUsageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

class AppUsageRepositoryImpl @Inject constructor(
    private val dataSource: UsageStatsDataSource,
    private val mapper: AppUsageMapper,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : AppUsageRepository {

    override suspend fun getTodayUsage(): List<AppUsage> = withContext(ioDispatcher) {
        val (start, end) = todayWindowMillis()
        mapper.toAppUsageList(dataSource.getForegroundUsageMillis(start, end))
            .filter { it.foregroundTimeMillis > 0L }
            .sortedByDescending { it.foregroundTimeMillis }
    }

    /** Local calendar day: midnight → now (Constitution-clarified "today"). */
    private fun todayWindowMillis(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zone)
        val startOfDay = now.toLocalDate().atStartOfDay(zone)
        return startOfDay.toInstant().toEpochMilli() to now.toInstant().toEpochMilli()
    }
}
