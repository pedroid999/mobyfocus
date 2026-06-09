package com.pedroid.mobyfocus.presentation.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pedroid.mobyfocus.R

/**
 * Circular app icon, or a letter placeholder when the icon is unavailable
 * (uninstalled-app edge). Shared by dashboard rows and the detail screen.
 */
@Composable
fun AppIconBadge(
    icon: ImageBitmap?,
    fallbackLabel: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    fallbackTextStyle: TextStyle = MaterialTheme.typography.titleMedium,
) {
    if (icon != null) {
        Image(
            bitmap = icon,
            contentDescription = stringResource(R.string.app_icon_content_description),
            modifier = modifier
                .size(size)
                .clip(CircleShape),
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clearAndSetSemantics { },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = fallbackLabel.firstOrNull()?.uppercase() ?: "?",
                style = fallbackTextStyle,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}
