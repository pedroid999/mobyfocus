package com.pedroid.mobyfocus.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pedroid.mobyfocus.data.local.dao.AppClassificationDao
import com.pedroid.mobyfocus.data.local.entity.AppClassificationEntity

/** Schema exported to `app/schemas/` for future migration tests (research R7). */
@Database(
    entities = [AppClassificationEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class MobyFocusDatabase : RoomDatabase() {
    abstract fun appClassificationDao(): AppClassificationDao
}
