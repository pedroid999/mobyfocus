package com.pedroid.mobyfocus.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pedroid.mobyfocus.presentation.dashboard.DashboardRoute
import com.pedroid.mobyfocus.presentation.permission.PermissionRoute

object MobyFocusDestinations {
    const val PERMISSION = "permission"
    const val DASHBOARD = "dashboard"
}

@Composable
fun MobyFocusNavHost(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = MobyFocusDestinations.PERMISSION,
        modifier = modifier,
    ) {
        composable(MobyFocusDestinations.PERMISSION) {
            PermissionRoute(
                contentPadding = contentPadding,
                onPermissionGranted = {
                    navController.navigate(MobyFocusDestinations.DASHBOARD) {
                        popUpTo(MobyFocusDestinations.PERMISSION) { inclusive = true }
                        launchSingleTop = true
                    }
                },
            )
        }
        composable(MobyFocusDestinations.DASHBOARD) {
            DashboardRoute(contentPadding = contentPadding)
        }
    }
}
