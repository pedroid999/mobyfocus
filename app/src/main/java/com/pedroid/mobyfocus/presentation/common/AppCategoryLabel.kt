package com.pedroid.mobyfocus.presentation.common

import androidx.annotation.StringRes
import com.pedroid.mobyfocus.R
import com.pedroid.mobyfocus.domain.model.AppCategory

/**
 * Category display names live in `strings.xml` (Principle V); this is the
 * single mapping shared by the detail screen and dashboard rows.
 */
@StringRes
fun AppCategory.labelRes(): Int = when (this) {
    AppCategory.PRODUCTIVE -> R.string.category_productive
    AppCategory.COMMUNICATION -> R.string.category_communication
    AppCategory.LEARNING -> R.string.category_learning
    AppCategory.ENTERTAINMENT -> R.string.category_entertainment
    AppCategory.SOCIAL -> R.string.category_social
    AppCategory.DISTRACTING -> R.string.category_distracting
    AppCategory.NEUTRAL -> R.string.category_neutral
}
