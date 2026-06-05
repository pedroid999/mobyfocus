package com.pedroid.mobyfocus.data.mapper

import android.content.Context
import android.content.pm.PackageManager
import com.pedroid.mobyfocus.domain.model.AppUsage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Maps raw `package → foreground-millis` data into [AppUsage] domain models.
 *
 * Resolves the display name only; the icon is resolved later by the presentation
 * layer (the domain stays framework-free — Principle III).
 */
class AppUsageMapper @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {

    fun toAppUsageList(usageByPackage: Map<String, Long>): List<AppUsage> {
        val packageManager = context.packageManager
        return usageByPackage
            .filter { (_, millis) -> millis > 0L }
            .map { (packageName, millis) ->
                AppUsage(
                    packageName = packageName,
                    displayName = resolveLabel(packageManager, packageName) ?: packageName,
                    foregroundTimeMillis = millis,
                )
            }
    }

    private fun resolveLabel(packageManager: PackageManager, packageName: String): String? =
        try {
            packageManager
                .getApplicationLabel(packageManager.getApplicationInfo(packageName, 0))
                .toString()
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
}
