package com.pedroid.mobyfocus.domain.usecase

import com.pedroid.mobyfocus.domain.model.AppCategory
import com.pedroid.mobyfocus.domain.model.ClassifiedAppUsage
import com.pedroid.mobyfocus.domain.repository.AppClassificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Joins today's one-shot usage snapshot with the reactive classification
 * stream (research R3). Usage order and filtering are preserved exactly
 * (SC-006); apps without a saved classification join to NEUTRAL (FR-008);
 * every classification change re-emits the joined list (FR-007). If the
 * classification stream fails, usage still renders with every row NEUTRAL
 * instead of erroring (FR-010).
 */
class GetClassifiedAppUsageUseCase @Inject constructor(
    private val getTodayAppUsage: GetTodayAppUsageUseCase,
    private val classificationRepository: AppClassificationRepository,
) {
    operator fun invoke(): Flow<List<ClassifiedAppUsage>> = flow {
        val usage = getTodayAppUsage()
        emitAll(
            classificationRepository.observeAll()
                .catch { emit(emptyList()) }
                .map { classifications ->
                val categoryByPackage =
                    classifications.associateBy({ it.packageName }, { it.category })
                usage.map { appUsage ->
                    ClassifiedAppUsage(
                        appUsage = appUsage,
                        category = categoryByPackage[appUsage.packageName]
                            ?: AppCategory.NEUTRAL,
                    )
                }
            },
        )
    }
}
