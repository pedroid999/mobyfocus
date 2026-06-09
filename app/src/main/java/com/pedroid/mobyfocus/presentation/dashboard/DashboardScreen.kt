package com.pedroid.mobyfocus.presentation.dashboard

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pedroid.mobyfocus.R
import com.pedroid.mobyfocus.domain.model.ClassifiedAppUsage
import com.pedroid.mobyfocus.presentation.common.rememberAppIcon
import com.pedroid.mobyfocus.presentation.common.usageLabel
import com.pedroid.mobyfocus.presentation.dashboard.components.AppUsageRow

@Composable
fun DashboardRoute(
    contentPadding: PaddingValues,
    onAppClick: (packageName: String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DashboardScreen(
        state = state,
        contentPadding = contentPadding,
        onRetry = viewModel::load,
        onAppClick = onAppClick,
    )
}

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onRetry: () -> Unit,
    onAppClick: (packageName: String) -> Unit = {},
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

            is DashboardUiState.Content -> UsageList(apps = state.apps, onAppClick = onAppClick)
        }
    }
}

@Composable
private fun UsageList(
    apps: List<ClassifiedAppUsage>,
    onAppClick: (packageName: String) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text(
                text = stringResource(R.string.dashboard_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp),
            )
        }
        items(items = apps, key = { it.appUsage.packageName }) { item ->
            AppUsageRow(
                item = item,
                usageLabel = usageLabel(item.appUsage.foregroundTimeMillis),
                icon = rememberAppIcon(item.appUsage.packageName),
                onClick = onAppClick,
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

