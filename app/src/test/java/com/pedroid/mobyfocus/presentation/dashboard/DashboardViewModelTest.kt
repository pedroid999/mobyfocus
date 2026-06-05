package com.pedroid.mobyfocus.presentation.dashboard

import app.cash.turbine.test
import com.pedroid.mobyfocus.domain.model.AppUsage
import com.pedroid.mobyfocus.domain.usecase.GetTodayAppUsageUseCase
import com.pedroid.mobyfocus.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getTodayAppUsage = mockk<GetTodayAppUsageUseCase>()

    @Test
    fun `emits Loading then Content when usage exists`() = runTest {
        val apps = listOf(AppUsage("a", "A", 500))
        coEvery { getTodayAppUsage() } returns apps

        val viewModel = DashboardViewModel(getTodayAppUsage)

        viewModel.uiState.test {
            assertEquals(DashboardUiState.Loading, awaitItem())
            assertEquals(DashboardUiState.Content(apps), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Loading then Empty when there is no usage`() = runTest {
        coEvery { getTodayAppUsage() } returns emptyList()

        val viewModel = DashboardViewModel(getTodayAppUsage)

        viewModel.uiState.test {
            assertEquals(DashboardUiState.Loading, awaitItem())
            assertEquals(DashboardUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Loading then Error when use case fails`() = runTest {
        coEvery { getTodayAppUsage() } throws RuntimeException("boom")

        val viewModel = DashboardViewModel(getTodayAppUsage)

        viewModel.uiState.test {
            assertEquals(DashboardUiState.Loading, awaitItem())
            assertTrue(awaitItem() is DashboardUiState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
