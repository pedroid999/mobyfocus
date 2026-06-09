package com.pedroid.mobyfocus.domain.model

/**
 * What dashboard rows and the detail screen present: an app's usage combined
 * with its effective category — the saved value, or [AppCategory.NEUTRAL]
 * when no classification exists (FR-008).
 */
data class ClassifiedAppUsage(
    val appUsage: AppUsage,
    val category: AppCategory,
)
