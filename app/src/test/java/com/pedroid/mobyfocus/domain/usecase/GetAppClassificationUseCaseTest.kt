package com.pedroid.mobyfocus.domain.usecase

import app.cash.turbine.test
import com.pedroid.mobyfocus.domain.model.AppCategory
import com.pedroid.mobyfocus.domain.model.AppClassification
import com.pedroid.mobyfocus.domain.repository.AppClassificationRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GetAppClassificationUseCaseTest {

    private val repository = mockk<AppClassificationRepository>()
    private val useCase = GetAppClassificationUseCase(repository)

    @Test
    fun `delegates to repository observeByPackageName and emits its values`() = runTest {
        val classification = AppClassification("com.app.a", AppCategory.LEARNING, 7L)
        every { repository.observeByPackageName("com.app.a") } returns
            flowOf(classification, null)

        useCase("com.app.a").test {
            assertEquals(classification, awaitItem())
            assertNull(awaitItem())
            awaitComplete()
        }
    }
}
