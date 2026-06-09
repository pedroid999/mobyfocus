package com.pedroid.mobyfocus.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedroid.mobyfocus.domain.model.AppCategory
import com.pedroid.mobyfocus.domain.model.AppClassification
import com.pedroid.mobyfocus.domain.model.AppUsage
import com.pedroid.mobyfocus.domain.usecase.GetAppClassificationUseCase
import com.pedroid.mobyfocus.domain.usecase.GetTodayAppUsageUseCase
import com.pedroid.mobyfocus.domain.usecase.SaveAppClassificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTodayAppUsage: GetTodayAppUsageUseCase,
    getAppClassification: GetAppClassificationUseCase,
    private val saveAppClassification: SaveAppClassificationUseCase,
) : ViewModel() {

    private val packageName: String = checkNotNull(savedStateHandle[ARG_PACKAGE_NAME])

    private val saveFailed = MutableStateFlow(false)

    /**
     * One-shot usage snapshot for this package. An app absent from today's
     * usage (e.g., uninstalled later today) falls back to the package name as
     * display name with zero usage (spec edge case).
     */
    private val usageSnapshot = flow { emit(loadUsage()) }

    val uiState: StateFlow<DetailUiState> = combine(
        usageSnapshot,
        getAppClassification(packageName),
        saveFailed,
        ::buildContent,
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = DetailUiState.Loading,
    )

    fun onCategorySelected(category: AppCategory) {
        viewModelScope.launch {
            try {
                saveAppClassification(packageName, category)
            } catch (_: Exception) {
                // FR-010: inform the user; the previously persisted category
                // stays in effect (the classification flow never re-emitted).
                saveFailed.value = true
            }
        }
    }

    fun onSaveErrorShown() {
        saveFailed.value = false
    }

    private suspend fun loadUsage(): AppUsage =
        getTodayAppUsage().firstOrNull { it.packageName == packageName }
            ?: AppUsage(
                packageName = packageName,
                displayName = packageName,
                foregroundTimeMillis = 0L,
            )

    private fun buildContent(
        usage: AppUsage,
        classification: AppClassification?,
        saveFailed: Boolean,
    ): DetailUiState = DetailUiState.Content(
        packageName = usage.packageName,
        displayName = usage.displayName,
        foregroundTimeMillis = usage.foregroundTimeMillis,
        category = classification?.category ?: AppCategory.NEUTRAL,
        saveFailed = saveFailed,
    )

    companion object {
        const val ARG_PACKAGE_NAME = "packageName"
        private const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
