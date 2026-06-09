package com.pedroid.mobyfocus.data.mapper

import com.pedroid.mobyfocus.data.local.entity.AppClassificationEntity
import com.pedroid.mobyfocus.domain.model.AppCategory
import com.pedroid.mobyfocus.domain.model.AppClassification
import javax.inject.Inject

/**
 * Entity ↔ domain mapping. An unknown stored category resolves to NEUTRAL
 * via [AppCategory.fromStorageKey] (corruption edge case).
 */
class AppClassificationMapper @Inject constructor() {

    fun toDomain(entity: AppClassificationEntity): AppClassification =
        AppClassification(
            packageName = entity.packageName,
            category = AppCategory.fromStorageKey(entity.category),
            updatedAtEpochMillis = entity.updatedAtEpochMillis,
        )

    fun toEntity(domain: AppClassification): AppClassificationEntity =
        AppClassificationEntity(
            packageName = domain.packageName,
            category = domain.category.storageKey,
            updatedAtEpochMillis = domain.updatedAtEpochMillis,
        )
}
