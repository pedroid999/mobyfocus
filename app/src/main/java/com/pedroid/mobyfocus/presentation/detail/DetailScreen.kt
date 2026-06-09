package com.pedroid.mobyfocus.presentation.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pedroid.mobyfocus.R
import com.pedroid.mobyfocus.domain.model.AppCategory
import com.pedroid.mobyfocus.presentation.common.AppIconBadge
import com.pedroid.mobyfocus.presentation.common.labelRes
import com.pedroid.mobyfocus.presentation.common.rememberAppIcon
import com.pedroid.mobyfocus.presentation.common.usageLabel

@Composable
fun DetailRoute(
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    viewModel: DetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DetailScreen(
        state = state,
        onCategorySelected = viewModel::onCategorySelected,
        onSaveErrorShown = viewModel::onSaveErrorShown,
        onNavigateBack = onNavigateBack,
        contentPadding = contentPadding,
    )
}

/** Stateless detail screen (UDF): state down, events up. Auto-save on tap — no save button. */
@Composable
fun DetailScreen(
    state: DetailUiState,
    onCategorySelected: (AppCategory) -> Unit,
    onSaveErrorShown: () -> Unit,
    onNavigateBack: () -> Unit,
    contentPadding: PaddingValues = PaddingValues(),
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val saveErrorMessage = stringResource(R.string.detail_save_error)
    val saveFailed = state is DetailUiState.Content && state.saveFailed

    // FR-010: a failed save informs the user; the flag is transient and
    // consumed once the message has been shown.
    LaunchedEffect(saveFailed) {
        if (saveFailed) {
            snackbarHostState.showSnackbar(saveErrorMessage)
            onSaveErrorShown()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription =
                            stringResource(R.string.detail_back_content_description),
                    )
                }
                Text(
                    text = stringResource(R.string.detail_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }

            when (state) {
                DetailUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

                is DetailUiState.Content -> DetailContent(
                    content = state,
                    onCategorySelected = onCategorySelected,
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun DetailContent(
    content: DetailUiState.Content,
    onCategorySelected: (AppCategory) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AppIconBadge(
                icon = rememberAppIcon(content.packageName),
                fallbackLabel = content.displayName,
                size = 56.dp,
                fallbackTextStyle = MaterialTheme.typography.titleLarge,
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = content.displayName,
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = content.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.detail_usage_today_label),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = usageLabel(content.foregroundTimeMillis),
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.detail_category_section_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(8.dp))
        Column(modifier = Modifier.selectableGroup()) {
            AppCategory.entries.forEach { category ->
                CategoryOption(
                    category = category,
                    selected = category == content.category,
                    onClick = { onCategorySelected(category) },
                )
            }
        }
    }
}

@Composable
private fun CategoryOption(
    category: AppCategory,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton,
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(12.dp))
        Text(
            text = stringResource(category.labelRes()),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
