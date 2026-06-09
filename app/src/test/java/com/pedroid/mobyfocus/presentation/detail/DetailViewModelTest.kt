package com.pedroid.mobyfocus.presentation.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.pedroid.mobyfocus.domain.model.AppCategory
import com.pedroid.mobyfocus.domain.model.AppClassification
import com.pedroid.mobyfocus.domain.model.AppUsage
import com.pedroid.mobyfocus.domain.usecase.GetAppClassificationUseCase
import com.pedroid.mobyfocus.domain.usecase.GetTodayAppUsageUseCase
import com.pedroid.mobyfocus.domain.usecase.SaveAppClassificationUseCase
import com.pedroid.mobyfocus.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val getTodayAppUsage = mockk<GetTodayAppUsageUseCase>()
    private val getAppClassification = mockk<GetAppClassificationUseCase>()
    private val saveAppClassification = mockk<SaveAppClassificationUseCase>()

    private val classificationFlow = MutableStateFlow<AppClassification?>(null)

    private fun viewModel(packageName: String = PKG) = DetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("packageName" to packageName)),
        getTodayAppUsage = getTodayAppUsage,
        getAppClassification = getAppClassification,
        saveAppClassification = saveAppClassification,
    )

    private fun givenUsage(vararg usage: AppUsage) {
        coEvery { getTodayAppUsage() } returns usage.toList()
        every { getAppClassification(PKG) } returns classificationFlow
    }

    @Test
    fun `emits Loading then Content with usage and NEUTRAL when unclassified`() = runTest {
        givenUsage(AppUsage(PKG, "App A", 120_000L))

        viewModel().uiState.test {
            assertEquals(DetailUiState.Loading, awaitItem())
            assertEquals(
                DetailUiState.Content(
                    packageName = PKG,
                    displayName = "App A",
                    foregroundTimeMillis = 120_000L,
                    category = AppCategory.NEUTRAL,
                    saveFailed = false,
                ),
                awaitItem(),
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `shows saved category when a classification exists`() = runTest {
        givenUsage(AppUsage(PKG, "App A", 120_000L))
        classificationFlow.value = AppClassification(PKG, AppCategory.SOCIAL, 1L)

        viewModel().uiState.test {
            assertEquals(DetailUiState.Loading, awaitItem())
            assertEquals(AppCategory.SOCIAL, (awaitItem() as DetailUiState.Content).category)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `falls back to package name and zero usage when app absent from today`() = runTest {
        givenUsage(AppUsage("com.other", "Other", 5_000L))

        viewModel().uiState.test {
            assertEquals(DetailUiState.Loading, awaitItem())
            val content = awaitItem() as DetailUiState.Content
            assertEquals(PKG, content.displayName)
            assertEquals(0L, content.foregroundTimeMillis)
            assertEquals(AppCategory.NEUTRAL, content.category)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `selecting a category saves it and content reflects the re-emitted classification`() =
        runTest {
            givenUsage(AppUsage(PKG, "App A", 120_000L))
            coEvery { saveAppClassification(PKG, AppCategory.PRODUCTIVE) } returns Unit

            val viewModel = viewModel()
            viewModel.uiState.test {
                assertEquals(DetailUiState.Loading, awaitItem())
                assertEquals(AppCategory.NEUTRAL, (awaitItem() as DetailUiState.Content).category)

                viewModel.onCategorySelected(AppCategory.PRODUCTIVE)
                classificationFlow.value = AppClassification(PKG, AppCategory.PRODUCTIVE, 2L)

                assertEquals(
                    AppCategory.PRODUCTIVE,
                    (awaitItem() as DetailUiState.Content).category,
                )
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `consecutive selections save in order and the last one wins`() = runTest {
        givenUsage(AppUsage(PKG, "App A", 120_000L))
        coEvery { saveAppClassification(PKG, any()) } returns Unit

        val viewModel = viewModel()
        viewModel.uiState.test {
            assertEquals(DetailUiState.Loading, awaitItem())
            assertEquals(AppCategory.NEUTRAL, (awaitItem() as DetailUiState.Content).category)

            viewModel.onCategorySelected(AppCategory.PRODUCTIVE)
            viewModel.onCategorySelected(AppCategory.SOCIAL)
            classificationFlow.value = AppClassification(PKG, AppCategory.SOCIAL, 3L)

            assertEquals(AppCategory.SOCIAL, (awaitItem() as DetailUiState.Content).category)
            cancelAndIgnoreRemainingEvents()
        }

        coVerifyOrder {
            saveAppClassification(PKG, AppCategory.PRODUCTIVE)
            saveAppClassification(PKG, AppCategory.SOCIAL)
        }
    }

    @Test
    fun `failed save flags saveFailed and keeps the previously persisted category`() = runTest {
        givenUsage(AppUsage(PKG, "App A", 120_000L))
        classificationFlow.value = AppClassification(PKG, AppCategory.SOCIAL, 1L)
        coEvery { saveAppClassification(PKG, AppCategory.PRODUCTIVE) } throws
            RuntimeException("disk full")

        val viewModel = viewModel()
        viewModel.uiState.test {
            assertEquals(DetailUiState.Loading, awaitItem())
            assertEquals(AppCategory.SOCIAL, (awaitItem() as DetailUiState.Content).category)

            viewModel.onCategorySelected(AppCategory.PRODUCTIVE)

            val afterFailure = awaitItem() as DetailUiState.Content
            assertEquals(true, afterFailure.saveFailed)
            assertEquals(AppCategory.SOCIAL, afterFailure.category)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSaveErrorShown consumes the transient saveFailed flag`() = runTest {
        givenUsage(AppUsage(PKG, "App A", 120_000L))
        coEvery { saveAppClassification(PKG, any()) } throws RuntimeException("disk full")

        val viewModel = viewModel()
        viewModel.uiState.test {
            assertEquals(DetailUiState.Loading, awaitItem())
            assertEquals(false, (awaitItem() as DetailUiState.Content).saveFailed)

            viewModel.onCategorySelected(AppCategory.PRODUCTIVE)
            assertEquals(true, (awaitItem() as DetailUiState.Content).saveFailed)

            viewModel.onSaveErrorShown()
            assertEquals(false, (awaitItem() as DetailUiState.Content).saveFailed)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private companion object {
        const val PKG = "com.app.a"
    }
}
