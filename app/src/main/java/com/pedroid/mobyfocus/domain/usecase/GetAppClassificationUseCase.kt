package com.pedroid.mobyfocus.domain.usecase

import com.pedroid.mobyfocus.domain.model.AppClassification
import com.pedroid.mobyfocus.domain.repository.AppClassificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/** Observes one app's classification; emits null while none is saved. */
class GetAppClassificationUseCase @Inject constructor(
    private val repository: AppClassificationRepository,
) {
    operator fun invoke(packageName: String): Flow<AppClassification?> =
        repository.observeByPackageName(packageName)
}
