package com.pedroid.mobyfocus.domain.usecase

import app.cash.turbine.test
import com.pedroid.mobyfocus.domain.model.AppCategory
import com.pedroid.mobyfocus.domain.model.AppClassification
import com.pedroid.mobyfocus.domain.model.AppUsage
import com.pedroid.mobyfocus.domain.model.ClassifiedAppUsage
import com.pedroid.mobyfocus.domain.repository.AppClassificationRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetClassifiedAppUsageUseCaseTest {

    private val getTodayAppUsage = mockk<GetTodayAppUsageUseCase>()
    private val classificationRepository = mockk<AppClassificationRepository>()
    private val useCase = GetClassifiedAppUsageUseCase(getTodayAppUsage, classificationRepository)

    private val usageA = AppUsage("com.app.a", "App A", 300_000L)
    private val usageB = AppUsage("com.app.b", "App B", 120_000L)
    private val usageC = AppUsage("com.app.c", "App C", 60_000L)

    @Test
    fun `joins usage with classifications by package name`() = runTest {
        coEvery { getTodayAppUsage() } returns listOf(usageA, usageB)
        every { classificationRepository.observeAll() } returns flowOf(
            listOf(AppClassification("com.app.a", AppCategory.PRODUCTIVE, 1L)),
        )

        useCase().test {
            assertEquals(
                listOf(
                    ClassifiedAppUsage(usageA, AppCategory.PRODUCTIVE),
                    ClassifiedAppUsage(usageB, AppCategory.NEUTRAL),
                ),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `apps without classification default to NEUTRAL`() = runTest {
        coEvery { getTodayAppUsage() } returns listOf(usageA, usageB, usageC)
        every { classificationRepository.observeAll() } returns flowOf(emptyList())

        useCase().test {
            val items = awaitItem()
            assertEquals(3, items.size)
            items.forEach { assertEquals(AppCategory.NEUTRAL, it.category) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `preserves usage list order and filtering exactly`() = runTest {
        coEvery { getTodayAppUsage() } returns listOf(usageA, usageB, usageC)
        every { classificationRepository.observeAll() } returns flowOf(
            listOf(AppClassification("com.app.c", AppCategory.SOCIAL, 1L)),
        )

        useCase().test {
            assertEquals(
                listOf(usageA, usageB, usageC),
                awaitItem().map { it.appUsage },
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `classification read failure still emits usage with all rows NEUTRAL`() = runTest {
        coEvery { getTodayAppUsage() } returns listOf(usageA, usageB)
        every { classificationRepository.observeAll() } returns
            kotlinx.coroutines.flow.flow { throw RuntimeException("storage corrupted") }

        useCase().test {
            assertEquals(
                listOf(
                    ClassifiedAppUsage(usageA, AppCategory.NEUTRAL),
                    ClassifiedAppUsage(usageB, AppCategory.NEUTRAL),
                ),
                awaitItem(),
            )
            awaitComplete()
        }
    }

    @Test
    fun `re-emits the joined list when a classification changes`() = runTest {
        coEvery { getTodayAppUsage() } returns listOf(usageA)
        val classifications =
            MutableStateFlow<List<AppClassification>>(emptyList())
        every { classificationRepository.observeAll() } returns classifications

        useCase().test {
            assertEquals(
                listOf(ClassifiedAppUsage(usageA, AppCategory.NEUTRAL)),
                awaitItem(),
            )

            classifications.value =
                listOf(AppClassification("com.app.a", AppCategory.DISTRACTING, 2L))

            assertEquals(
                listOf(ClassifiedAppUsage(usageA, AppCategory.DISTRACTING)),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
