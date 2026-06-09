package com.pedroid.mobyfocus.presentation.detail

import com.pedroid.mobyfocus.domain.model.AppCategory

/** Immutable detail-screen state exposed via StateFlow (Principle IV). */
sealed interface DetailUiState {
    data object Loading : DetailUiState

    data class Content(
        val packageName: String,
        val displayName: String,
        val foregroundTimeMillis: Long,
        val category: AppCategory,
        val saveFailed: Boolean,
    ) : DetailUiState
}
