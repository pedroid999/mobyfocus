package com.pedroid.mobyfocus.presentation.dashboard

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.pedroid.mobyfocus.R
import com.pedroid.mobyfocus.domain.model.AppUsage
import com.pedroid.mobyfocus.ui.theme.MobyFocusTheme
import org.junit.Rule
import org.junit.Test

class DashboardScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun emptyState_showsEmptyMessage() {
        composeRule.setContent {
            MobyFocusTheme {
                DashboardScreen(state = DashboardUiState.Empty, onRetry = {})
            }
        }

        val message = composeRule.activity.getString(R.string.dashboard_empty)
        composeRule.onNodeWithText(message).assertIsDisplayed()
    }

    @Test
    fun contentState_showsAppNameAndPackage() {
        val app = AppUsage("com.app.a", "App A", 120_000L)
        composeRule.setContent {
            MobyFocusTheme {
                DashboardScreen(state = DashboardUiState.Content(listOf(app)), onRetry = {})
            }
        }

        composeRule.onNodeWithText("App A").assertIsDisplayed()
        composeRule.onNodeWithText("com.app.a").assertIsDisplayed()
    }

    @Test
    fun errorState_showsRetryButton() {
        composeRule.setContent {
            MobyFocusTheme {
                DashboardScreen(state = DashboardUiState.Error, onRetry = {})
            }
        }

        val retry = composeRule.activity.getString(R.string.dashboard_retry)
        composeRule.onNodeWithText(retry).assertIsDisplayed()
    }
}
