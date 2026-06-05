package com.pedroid.mobyfocus.domain.repository

import com.pedroid.mobyfocus.domain.model.UsageAccessPermissionStatus

/** Reads the current Usage Access permission state from the OS (never cached). */
interface UsageAccessRepository {
    fun getPermissionStatus(): UsageAccessPermissionStatus
}
