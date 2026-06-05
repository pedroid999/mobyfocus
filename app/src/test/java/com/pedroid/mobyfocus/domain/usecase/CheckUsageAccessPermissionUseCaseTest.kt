package com.pedroid.mobyfocus.domain.usecase

import com.pedroid.mobyfocus.domain.model.UsageAccessPermissionStatus
import com.pedroid.mobyfocus.domain.repository.UsageAccessRepository
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class CheckUsageAccessPermissionUseCaseTest {

    private val repository = mockk<UsageAccessRepository>()
    private val useCase = CheckUsageAccessPermissionUseCase(repository)

    @Test
    fun `returns GRANTED when repository reports granted`() {
        every { repository.getPermissionStatus() } returns UsageAccessPermissionStatus.GRANTED

        assertEquals(UsageAccessPermissionStatus.GRANTED, useCase())
    }

    @Test
    fun `returns NOT_GRANTED when repository reports not granted`() {
        every { repository.getPermissionStatus() } returns UsageAccessPermissionStatus.NOT_GRANTED

        assertEquals(UsageAccessPermissionStatus.NOT_GRANTED, useCase())
    }
}
