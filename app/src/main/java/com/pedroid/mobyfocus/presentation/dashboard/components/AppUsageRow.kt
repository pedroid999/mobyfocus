package com.pedroid.mobyfocus.presentation.dashboard.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.pedroid.mobyfocus.domain.model.ClassifiedAppUsage
import com.pedroid.mobyfocus.presentation.common.AppIconBadge
import com.pedroid.mobyfocus.presentation.common.labelRes

/**
 * Stateless row: icon (or letter placeholder), display name + package name,
 * usage label and the app's effective category (FR-007/FR-008). The icon is
 * resolved by the screen and passed in as an [ImageBitmap]. Tapping the row
 * opens the app's detail screen (FR-003).
 */
@Composable
fun AppUsageRow(
    item: ClassifiedAppUsage,
    usageLabel: String,
    icon: ImageBitmap?,
    onClick: (packageName: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val app = item.appUsage
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(app.packageName) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIconBadge(icon = icon, fallbackLabel = app.displayName)

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.displayName,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = usageLabel,
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = stringResource(item.category.labelRes()),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
