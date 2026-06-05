package com.pedroid.mobyfocus.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedroid.mobyfocus.domain.usecase.GetTodayAppUsageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getTodayAppUsage: GetTodayAppUsageUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        _uiState.value = DashboardUiState.Loading
        viewModelScope.launch {
            _uiState.value = try {
                val apps = getTodayAppUsage()
                if (apps.isEmpty()) DashboardUiState.Empty else DashboardUiState.Content(apps)
            } catch (_: Exception) {
                DashboardUiState.Error
            }
        }
    }
}
