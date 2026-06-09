package com.pedroid.mobyfocus.domain.model

/**
 * The closed set of seven attention categories (FR-001). [NEUTRAL] is the
 * universal default for apps without a saved classification (FR-008).
 *
 * Display names live in `strings.xml`, resolved by the presentation layer
 * (Principle V) — the domain only knows the stable [storageKey].
 */
enum class AppCategory {
    PRODUCTIVE,
    COMMUNICATION,
    LEARNING,
    ENTERTAINMENT,
    SOCIAL,
    DISTRACTING,
    NEUTRAL;

    /** Stable persistence identifier; never derived from UI text. */
    val storageKey: String get() = name

    companion object {
        /** Unknown/corrupted keys resolve to [NEUTRAL] (spec edge case). */
        fun fromStorageKey(key: String): AppCategory =
            entries.firstOrNull { it.storageKey == key } ?: NEUTRAL
    }
}
