package com.pedroid.mobyfocus.domain.usecase

import com.pedroid.mobyfocus.domain.model.AppUsage
import com.pedroid.mobyfocus.domain.repository.AppUsageRepository
import javax.inject.Inject

class GetTodayAppUsageUseCase @Inject constructor(
    private val repository: AppUsageRepository,
) {
    suspend operator fun invoke(): List<AppUsage> =
        repository.getTodayUsage()
            .filter { it.foregroundTimeMillis > 0L }
            .sortedByDescending { it.foregroundTimeMillis }
}
