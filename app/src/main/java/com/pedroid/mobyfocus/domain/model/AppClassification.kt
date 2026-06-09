package com.pedroid.mobyfocus.domain.model

/**
 * The association between one app and one [AppCategory] (at most one per
 * package; saving again replaces the previous value). Absence of a
 * classification is modeled by no record — presented as [AppCategory.NEUTRAL].
 */
data class AppClassification(
    val packageName: String,
    val category: AppCategory,
    val updatedAtEpochMillis: Long,
)
