package com.pedroid.mobyfocus.domain.model

/**
 * A single app's foreground attention summary for today.
 *
 * Icon-agnostic by design (Constitution Principle III): the domain never holds an
 * Android type. The presentation layer resolves the icon by [packageName].
 */
data class AppUsage(
    val packageName: String,
    val displayName: String,
    val foregroundTimeMillis: Long,
)
