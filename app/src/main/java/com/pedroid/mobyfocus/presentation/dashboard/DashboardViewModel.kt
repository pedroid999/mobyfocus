package com.pedroid.mobyfocus.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pedroid.mobyfocus.domain.usecase.GetClassifiedAppUsageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getClassifiedAppUsage: GetClassifiedAppUsageUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        load()
    }

    fun load() {
        loadJob?.cancel()
        _uiState.value = DashboardUiState.Loading
        loadJob = viewModelScope.launch {
            getClassifiedAppUsage()
                .catch { _uiState.value = DashboardUiState.Error }
                .collect { apps ->
                    _uiState.value =
                        if (apps.isEmpty()) DashboardUiState.Empty
                        else DashboardUiState.Content(apps)
                }
        }
    }
}
