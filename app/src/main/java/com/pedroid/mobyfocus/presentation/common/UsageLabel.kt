package com.pedroid.mobyfocus.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.pedroid.mobyfocus.R

private const val MILLIS_PER_MINUTE = 60_000L

/** Locale-aware usage label shared by dashboard rows and the detail screen. */
@Composable
fun usageLabel(foregroundTimeMillis: Long): String =
    if (foregroundTimeMillis < MILLIS_PER_MINUTE) {
        stringResource(R.string.usage_less_than_minute)
    } else {
        stringResource(R.string.usage_minutes, (foregroundTimeMillis / MILLIS_PER_MINUTE).toInt())
    }
