package com.pedroid.mobyfocus.presentation.permission

import com.pedroid.mobyfocus.domain.model.UsageAccessPermissionStatus

data class PermissionUiState(
    val status: UsageAccessPermissionStatus = UsageAccessPermissionStatus.NOT_GRANTED,
)
