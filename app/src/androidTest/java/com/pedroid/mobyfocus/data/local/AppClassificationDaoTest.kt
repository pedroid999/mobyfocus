package com.pedroid.mobyfocus.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.pedroid.mobyfocus.data.local.dao.AppClassificationDao
import com.pedroid.mobyfocus.data.local.entity.AppClassificationEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** In-memory Room round-trip for the generated DAO (research R8). */
@RunWith(AndroidJUnit4::class)
class AppClassificationDaoTest {

    private lateinit var database: MobyFocusDatabase
    private lateinit var dao: AppClassificationDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, MobyFocusDatabase::class.java).build()
        dao = database.appClassificationDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun upsert_insertsNewRow() = runTest {
        val entity = AppClassificationEntity("com.app.a", "PRODUCTIVE", 1L)

        dao.upsert(entity)

        assertEquals(listOf(entity), dao.observeAll().first())
    }

    @Test
    fun upsert_withSamePackageName_replacesRow_lastWriteWins() = runTest {
        dao.upsert(AppClassificationEntity("com.app.a", "PRODUCTIVE", 1L))
        dao.upsert(AppClassificationEntity("com.app.a", "SOCIAL", 2L))

        assertEquals(
            listOf(AppClassificationEntity("com.app.a", "SOCIAL", 2L)),
            dao.observeAll().first(),
        )
    }

    @Test
    fun observeAll_reEmitsAfterChange() = runTest {
        dao.observeAll().test {
            assertEquals(emptyList<AppClassificationEntity>(), awaitItem())

            dao.upsert(AppClassificationEntity("com.app.a", "LEARNING", 1L))

            assertEquals(
                listOf(AppClassificationEntity("com.app.a", "LEARNING", 1L)),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeByPackageName_emitsNullWhenAbsent() = runTest {
        assertNull(dao.observeByPackageName("com.absent").first())
    }

    @Test
    fun observeByPackageName_emitsRowWhenPresent() = runTest {
        val entity = AppClassificationEntity("com.app.a", "DISTRACTING", 3L)
        dao.upsert(entity)

        assertEquals(entity, dao.observeByPackageName("com.app.a").first())
    }
}
