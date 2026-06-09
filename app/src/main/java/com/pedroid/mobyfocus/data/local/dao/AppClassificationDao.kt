package com.pedroid.mobyfocus.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.pedroid.mobyfocus.data.local.entity.AppClassificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppClassificationDao {

    @Query("SELECT * FROM app_classifications")
    fun observeAll(): Flow<List<AppClassificationEntity>>

    @Query("SELECT * FROM app_classifications WHERE packageName = :packageName")
    fun observeByPackageName(packageName: String): Flow<AppClassificationEntity?>

    /**
     * Atomic insert-or-update (research R2) — avoids REPLACE's delete+insert
     * semantics, so observers of other rows are not needlessly retriggered.
     */
    @Upsert
    suspend fun upsert(entity: AppClassificationEntity)
}
