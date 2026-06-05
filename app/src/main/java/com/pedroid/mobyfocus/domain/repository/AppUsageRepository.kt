package com.pedroid.mobyfocus.domain.repository

import com.pedroid.mobyfocus.domain.model.AppUsage

/** Provides today's per-app foreground usage, already filtered and sorted. */
interface AppUsageRepository {
    /**
     * Today's usage with zero-usage entries removed and ordered by foreground
     * time descending.
     *
     * @throws Exception on an unrecoverable read failure (surfaced as Error state).
     */
    suspend fun getTodayUsage(): List<AppUsage>
}
