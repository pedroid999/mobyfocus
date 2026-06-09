package com.pedroid.mobyfocus.presentation.common

import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap

/**
 * Resolves an app icon by package name, or null when the app is not installed
 * (uninstalled-app edge case → callers render a generic fallback). The icon is
 * intentionally resolved in the presentation layer — never part of UiState
 * (contract §6 note).
 */
@Composable
fun rememberAppIcon(packageName: String): ImageBitmap? {
    val context = LocalContext.current
    return remember(packageName) {
        try {
            context.packageManager.getApplicationIcon(packageName).toBitmap().asImageBitmap()
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }
}
