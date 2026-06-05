package com.pedroid.mobyfocus.domain.usecase

import com.pedroid.mobyfocus.domain.model.UsageAccessPermissionStatus
import com.pedroid.mobyfocus.domain.repository.UsageAccessRepository
import javax.inject.Inject

class CheckUsageAccessPermissionUseCase @Inject constructor(
    private val repository: UsageAccessRepository,
) {
    operator fun invoke(): UsageAccessPermissionStatus = repository.getPermissionStatus()
}
