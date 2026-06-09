package com.pedroid.mobyfocus.domain.usecase

import com.pedroid.mobyfocus.domain.model.AppCategory
import com.pedroid.mobyfocus.domain.repository.AppClassificationRepository
import javax.inject.Inject

/** Persists a category for an app; failures propagate to the caller (FR-010). */
class SaveAppClassificationUseCase @Inject constructor(
    private val repository: AppClassificationRepository,
) {
    suspend operator fun invoke(packageName: String, category: AppCategory) =
        repository.save(packageName, category)
}
