package com.pedroid.mobyfocus.domain.repository

import com.pedroid.mobyfocus.domain.model.AppCategory
import com.pedroid.mobyfocus.domain.model.AppClassification
import kotlinx.coroutines.flow.Flow

interface AppClassificationRepository {
    /** All saved classifications; re-emits whenever any row changes. */
    fun observeAll(): Flow<List<AppClassification>>

    /** The classification for [packageName], or null when none saved; reactive. */
    fun observeByPackageName(packageName: String): Flow<AppClassification?>

    /**
     * Insert-or-replace the classification for [packageName] with [category],
     * stamped with the current time by the implementation.
     *
     * @throws Exception on an unrecoverable write failure (ViewModel catch boundary, FR-010).
     */
    suspend fun save(packageName: String, category: AppCategory)
}
