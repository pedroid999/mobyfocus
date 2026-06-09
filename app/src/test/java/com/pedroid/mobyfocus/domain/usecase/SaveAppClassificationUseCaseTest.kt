package com.pedroid.mobyfocus.domain.usecase

import com.pedroid.mobyfocus.domain.model.AppCategory
import com.pedroid.mobyfocus.domain.repository.AppClassificationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test

class SaveAppClassificationUseCaseTest {

    private val repository = mockk<AppClassificationRepository>()
    private val useCase = SaveAppClassificationUseCase(repository)

    @Test
    fun `delegates to repository save`() = runTest {
        coEvery { repository.save("com.app.a", AppCategory.PRODUCTIVE) } returns Unit

        useCase("com.app.a", AppCategory.PRODUCTIVE)

        coVerify(exactly = 1) { repository.save("com.app.a", AppCategory.PRODUCTIVE) }
    }

    @Test
    fun `propagates repository failure to the caller`() {
        coEvery { repository.save(any(), any()) } throws RuntimeException("write failed")

        assertThrows(RuntimeException::class.java) {
            runBlocking { useCase("com.app.a", AppCategory.SOCIAL) }
        }
    }
}
