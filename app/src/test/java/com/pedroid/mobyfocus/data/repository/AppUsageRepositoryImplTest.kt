package com.pedroid.mobyfocus.data.repository

import com.pedroid.mobyfocus.data.mapper.AppUsageMapper
import com.pedroid.mobyfocus.data.usage.UsageStatsDataSource
import com.pedroid.mobyfocus.domain.model.AppUsage
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppUsageRepositoryImplTest {

    private val dataSource = mockk<UsageStatsDataSource>()
    private val mapper = mockk<AppUsageMapper>()

    @Test
    fun `composes data source and mapper, filters zero and sorts descending`() = runTest {
        every { dataSource.getForegroundUsageMillis(any(), any()) } returns
            mapOf("a" to 500L, "b" to 100L, "z" to 0L)
        every { mapper.toAppUsageList(any()) } returns listOf(
            AppUsage("b", "B", 100),
            AppUsage("z", "Z", 0),
            AppUsage("a", "A", 500),
        )
        val repository = AppUsageRepositoryImpl(dataSource, mapper, UnconfinedTestDispatcher())

        val result = repository.getTodayUsage()

        assertEquals(listOf("a", "b"), result.map { it.packageName })
    }
}
