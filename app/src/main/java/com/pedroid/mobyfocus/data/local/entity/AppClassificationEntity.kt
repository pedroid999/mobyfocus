package com.pedroid.mobyfocus.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Row of `app_classifications`. [category] stores the domain
 * `AppCategory.storageKey`; mapping back tolerates unknown values
 * (corruption edge case → NEUTRAL).
 */
@Entity(tableName = "app_classifications")
data class AppClassificationEntity(
    @PrimaryKey val packageName: String,
    val category: String,
    val updatedAtEpochMillis: Long,
)
