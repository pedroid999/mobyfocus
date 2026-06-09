package com.pedroid.mobyfocus.presentation.dashboard

import app.cash.turbine.test
import com.pedroid.mobyfocus.domain.model.AppCategory
import com.pedroid.mobyfocus.domain.model.AppUsage
import com.pedroid.mobyfocus.domain.model.ClassifiedAppUsage
import com.pedroid.mobyfocus.domain.usecase.GetClassifiedAppUsageUseCase
import com.pedroid.mobyfocus.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getClassifiedAppUsage = mockk<GetClassifiedAppUsageUseCase>()

    private val classifiedApp =
        ClassifiedAppUsage(AppUsage("a", "A", 500), AppCategory.PRODUCTIVE)
    private val neutralApp =
        ClassifiedAppUsage(AppUsage("b", "B", 300), AppCategory.NEUTRAL)

    @Test
    fun `emits Loading then Content carrying classified apps`() = runTest {
        every { getClassifiedAppUsage() } returns flowOf(listOf(classifiedApp, neutralApp))

        val viewModel = DashboardViewModel(getClassifiedAppUsage)

        viewModel.uiState.test {
            assertEquals(DashboardUiState.Loading, awaitItem())
            val content = awaitItem() as DashboardUiState.Content
            assertEquals(AppCategory.PRODUCTIVE, content.apps[0].category)
            assertEquals(AppCategory.NEUTRAL, content.apps[1].category)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Loading then Empty when there is no usage`() = runTest {
        every { getClassifiedAppUsage() } returns flowOf(emptyList())

        val viewModel = DashboardViewModel(getClassifiedAppUsage)

        viewModel.uiState.test {
            assertEquals(DashboardUiState.Loading, awaitItem())
            assertEquals(DashboardUiState.Empty, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits Loading then Error when the stream fails`() = runTest {
        every { getClassifiedAppUsage() } returns flow { throw RuntimeException("boom") }

        val viewModel = DashboardViewModel(getClassifiedAppUsage)

        viewModel.uiState.test {
            assertEquals(DashboardUiState.Loading, awaitItem())
            assertTrue(awaitItem() is DashboardUiState.Error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `re-emits Content when a classification changes without reloading usage`() = runTest {
        val stream = MutableSharedFlow<List<ClassifiedAppUsage>>()
        every { getClassifiedAppUsage() } returns stream

        val viewModel = DashboardViewModel(getClassifiedAppUsage)
        // Let the ViewModel's collector start before emitting into the hot flow.
        mainDispatcherRule.testDispatcher.scheduler.runCurrent()

        viewModel.uiState.test {
            assertEquals(DashboardUiState.Loading, awaitItem())

            stream.emit(listOf(neutralApp))
            assertEquals(DashboardUiState.Content(listOf(neutralApp)), awaitItem())

            val reclassified = neutralApp.copy(category = AppCategory.DISTRACTING)
            stream.emit(listOf(reclassified))
            assertEquals(DashboardUiState.Content(listOf(reclassified)), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }

        verify(exactly = 1) { getClassifiedAppUsage() }
    }
}
