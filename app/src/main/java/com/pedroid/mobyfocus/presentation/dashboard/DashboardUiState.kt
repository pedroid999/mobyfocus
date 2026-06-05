package com.pedroid.mobyfocus.presentation.dashboard

import com.pedroid.mobyfocus.domain.model.AppUsage

/** Immutable dashboard state exposed via StateFlow (Principle IV). */
sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data class Content(val apps: List<AppUsage>) : DashboardUiState
    data object Empty : DashboardUiState
    data object Error : DashboardUiState
}
