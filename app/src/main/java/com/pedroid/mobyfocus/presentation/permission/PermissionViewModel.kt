package com.pedroid.mobyfocus.presentation.permission

import androidx.lifecycle.ViewModel
import com.pedroid.mobyfocus.domain.usecase.CheckUsageAccessPermissionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val checkUsageAccessPermission: CheckUsageAccessPermissionUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(PermissionUiState())
    val uiState: StateFlow<PermissionUiState> = _uiState.asStateFlow()

    /** Re-reads the live permission status (called on ON_RESUME). */
    fun refresh() {
        _uiState.update { it.copy(status = checkUsageAccessPermission()) }
    }
}
