package com.pedroid.mobyfocus.data.repository

import com.pedroid.mobyfocus.data.local.dao.AppClassificationDao
import com.pedroid.mobyfocus.data.local.entity.AppClassificationEntity
import com.pedroid.mobyfocus.data.mapper.AppClassificationMapper
import com.pedroid.mobyfocus.di.CurrentTimeMillis
import com.pedroid.mobyfocus.domain.model.AppCategory
import com.pedroid.mobyfocus.domain.model.AppClassification
import com.pedroid.mobyfocus.domain.repository.AppClassificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Room-backed [AppClassificationRepository]. The save timestamp comes from an
 * injected clock so unit tests can assert it deterministically (research R6).
 */
class AppClassificationRepositoryImpl @Inject constructor(
    private val dao: AppClassificationDao,
    private val mapper: AppClassificationMapper,
    @param:CurrentTimeMillis private val currentTimeMillis: () -> Long,
) : AppClassificationRepository {

    override fun observeAll(): Flow<List<AppClassification>> =
        dao.observeAll().map { entities -> entities.map(mapper::toDomain) }

    override fun observeByPackageName(packageName: String): Flow<AppClassification?> =
        dao.observeByPackageName(packageName).map { entity -> entity?.let(mapper::toDomain) }

    override suspend fun save(packageName: String, category: AppCategory) {
        dao.upsert(
            AppClassificationEntity(
                packageName = packageName,
                category = category.storageKey,
                updatedAtEpochMillis = currentTimeMillis(),
            ),
        )
    }
}
