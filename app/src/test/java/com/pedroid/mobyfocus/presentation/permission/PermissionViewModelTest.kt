package com.pedroid.mobyfocus.presentation.permission

import app.cash.turbine.test
import com.pedroid.mobyfocus.domain.model.UsageAccessPermissionStatus
import com.pedroid.mobyfocus.domain.usecase.CheckUsageAccessPermissionUseCase
import com.pedroid.mobyfocus.util.MainDispatcherRule
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PermissionViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val checkPermission = mockk<CheckUsageAccessPermissionUseCase>()

    @Test
    fun `initial state is NOT_GRANTED`() = runTest {
        every { checkPermission() } returns UsageAccessPermissionStatus.NOT_GRANTED
        val viewModel = PermissionViewModel(checkPermission)

        assertEquals(UsageAccessPermissionStatus.NOT_GRANTED, viewModel.uiState.value.status)
    }

    @Test
    fun `refresh transitions to GRANTED when permission becomes granted`() = runTest {
        every { checkPermission() } returns UsageAccessPermissionStatus.GRANTED
        val viewModel = PermissionViewModel(checkPermission)

        viewModel.uiState.test {
            assertEquals(UsageAccessPermissionStatus.NOT_GRANTED, awaitItem().status)
            viewModel.refresh()
            assertEquals(UsageAccessPermissionStatus.GRANTED, awaitItem().status)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh stays NOT_GRANTED when permission not granted`() = runTest {
        every { checkPermission() } returns UsageAccessPermissionStatus.NOT_GRANTED
        val viewModel = PermissionViewModel(checkPermission)

        viewModel.uiState.test {
            assertEquals(UsageAccessPermissionStatus.NOT_GRANTED, awaitItem().status)
            viewModel.refresh()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
