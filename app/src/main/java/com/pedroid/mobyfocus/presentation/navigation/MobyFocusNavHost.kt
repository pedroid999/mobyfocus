package com.pedroid.mobyfocus.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.pedroid.mobyfocus.presentation.dashboard.DashboardRoute
import com.pedroid.mobyfocus.presentation.detail.DetailRoute
import com.pedroid.mobyfocus.presentation.detail.DetailViewModel
import com.pedroid.mobyfocus.presentation.permission.PermissionRoute

object MobyFocusDestinations {
    const val PERMISSION = "permission"
    const val DASHBOARD = "dashboard"

    /** Contract §5: `detail/{packageName}`, single required string argument. */
    const val DETAIL = "detail/{${DetailViewModel.ARG_PACKAGE_NAME}}"

    fun detail(packageName: String) = "detail/$packageName"
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
            DashboardRoute(
                contentPadding = contentPadding,
                onAppClick = { packageName ->
                    navController.navigate(MobyFocusDestinations.detail(packageName))
                },
            )
        }
        composable(
            route = MobyFocusDestinations.DETAIL,
            arguments = listOf(
                navArgument(DetailViewModel.ARG_PACKAGE_NAME) { type = NavType.StringType },
            ),
        ) {
            DetailRoute(
                contentPadding = contentPadding,
                onNavigateBack = { navController.popBackStack() },
            )
        }
    }
}
