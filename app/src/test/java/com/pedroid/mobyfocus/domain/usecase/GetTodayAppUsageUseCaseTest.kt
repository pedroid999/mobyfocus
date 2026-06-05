package com.pedroid.mobyfocus.domain.usecase

import com.pedroid.mobyfocus.domain.model.AppUsage
import com.pedroid.mobyfocus.domain.repository.AppUsageRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetTodayAppUsageUseCaseTest {

    private val repository = mockk<AppUsageRepository>()
    private val useCase = GetTodayAppUsageUseCase(repository)

    @Test
    fun `sorts by foreground time descending and drops zero usage`() = runTest {
        coEvery { repository.getTodayUsage() } returns listOf(
            AppUsage("b", "B", 100),
            AppUsage("z", "Z", 0),
            AppUsage("a", "A", 500),
        )

        val result = useCase()

        assertEquals(listOf("a", "b"), result.map { it.packageName })
    }

    @Test
    fun `returns empty list when there is no usage`() = runTest {
        coEvery { repository.getTodayUsage() } returns emptyList()

        assertTrue(useCase().isEmpty())
    }
}
