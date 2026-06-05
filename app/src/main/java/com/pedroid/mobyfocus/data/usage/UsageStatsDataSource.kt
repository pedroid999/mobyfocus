package com.pedroid.mobyfocus.data.usage

import android.app.usage.UsageStatsManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Raw aggregated foreground millis per package for a time window. */
interface UsageStatsDataSource {
    fun getForegroundUsageMillis(startMillis: Long, endMillis: Long): Map<String, Long>
}

class UsageStatsDataSourceImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : UsageStatsDataSource {

    override fun getForegroundUsageMillis(startMillis: Long, endMillis: Long): Map<String, Long> {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        return usageStatsManager
            .queryAndAggregateUsageStats(startMillis, endMillis)
            .mapValues { (_, stats) -> stats.totalTimeInForeground }
    }
}
