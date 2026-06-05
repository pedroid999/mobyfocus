package com.pedroid.mobyfocus.presentation.dashboard

import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pedroid.mobyfocus.R
import com.pedroid.mobyfocus.domain.model.AppUsage
import com.pedroid.mobyfocus.presentation.dashboard.components.AppUsageRow

@Composable
fun DashboardRoute(
    contentPadding: PaddingValues,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(
        state = state,
        contentPadding = contentPadding,
        onRetry = viewModel::load,
    )
}

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onRetry: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().padding(contentPadding)) {
        when (state) {
            DashboardUiState.Loading -> CenteredContent {
                CircularProgressIndicator()
            }

            DashboardUiState.Empty -> CenteredMessage(stringResource(R.string.dashboard_empty))

            DashboardUiState.Error -> CenteredContent {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.dashboard_error),
                        textAlign = TextAlign.Center,
                    )
                    Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
                        Text(text = stringResource(R.string.dashboard_retry))
                    }
                }
            }

            is DashboardUiState.Content -> UsageList(apps = state.apps)
        }
    }
}

@Composable
private fun UsageList(apps: List<AppUsage>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = stringResource(R.string.dashboard_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp),
            )
        }
        items(items = apps, key = { it.packageName }) { app ->
            AppUsageRow(
                app = app,
                usageLabel = usageLabel(app.foregroundTimeMillis),
                icon = rememberAppIcon(app.packageName),
            )
        }
    }
}

@Composable
private fun CenteredContent(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        content()
    }
}

@Composable
private fun CenteredMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = message, textAlign = TextAlign.Center)
    }
}

@Composable
private fun usageLabel(foregroundTimeMillis: Long): String =
    if (foregroundTimeMillis < MILLIS_PER_MINUTE) {
        stringResource(R.string.usage_less_than_minute)
    } else {
        stringResource(R.string.usage_minutes, (foregroundTimeMillis / MILLIS_PER_MINUTE).toInt())
    }

@Composable
private fun rememberAppIcon(packageName: String): ImageBitmap? {
    val context = LocalContext.current
    return remember(packageName) {
        try {
            context.packageManager.getApplicationIcon(packageName).toBitmap().asImageBitmap()
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }
}

private const val MILLIS_PER_MINUTE = 60_000L
