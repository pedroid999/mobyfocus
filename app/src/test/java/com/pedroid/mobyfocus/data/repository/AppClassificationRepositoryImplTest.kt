package com.pedroid.mobyfocus.data.repository

import app.cash.turbine.test
import com.pedroid.mobyfocus.data.local.dao.AppClassificationDao
import com.pedroid.mobyfocus.data.local.entity.AppClassificationEntity
import com.pedroid.mobyfocus.data.mapper.AppClassificationMapper
import com.pedroid.mobyfocus.domain.model.AppCategory
import com.pedroid.mobyfocus.domain.model.AppClassification
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class AppClassificationRepositoryImplTest {

    private val dao = mockk<AppClassificationDao>()
    private val fixedNowMillis = 1_234L
    private val repository = AppClassificationRepositoryImpl(
        dao = dao,
        mapper = AppClassificationMapper(),
        currentTimeMillis = { fixedNowMillis },
    )

    @Test
    fun `observeAll maps entities to domain`() = runTest {
        every { dao.observeAll() } returns flowOf(
            listOf(AppClassificationEntity("com.app.a", "SOCIAL", 7L)),
        )

        repository.observeAll().test {
            assertEquals(
                listOf(AppClassification("com.app.a", AppCategory.SOCIAL, 7L)),
                awaitItem(),
            )
            awaitComplete()
        }
    }

    @Test
    fun `observeByPackageName maps entity and emits null when absent`() = runTest {
        every { dao.observeByPackageName("com.app.a") } returns flowOf(
            AppClassificationEntity("com.app.a", "LEARNING", 7L),
            null,
        )

        repository.observeByPackageName("com.app.a").test {
            assertEquals(
                AppClassification("com.app.a", AppCategory.LEARNING, 7L),
                awaitItem(),
            )
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `save upserts entity stamped with injected clock time`() = runTest {
        val saved = slot<AppClassificationEntity>()
        coEvery { dao.upsert(capture(saved)) } returns Unit

        repository.save("com.app.a", AppCategory.PRODUCTIVE)

        coVerify(exactly = 1) { dao.upsert(any()) }
        assertEquals(
            AppClassificationEntity("com.app.a", "PRODUCTIVE", fixedNowMillis),
            saved.captured,
        )
    }

    @Test
    fun `save propagates DAO failure`() = runTest {
        coEvery { dao.upsert(any()) } throws RuntimeException("disk full")

        assertThrows(RuntimeException::class.java) {
            kotlinx.coroutines.runBlocking {
                repository.save("com.app.a", AppCategory.PRODUCTIVE)
            }
        }
    }
}
