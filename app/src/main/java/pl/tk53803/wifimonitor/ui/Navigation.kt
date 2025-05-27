package pl.tk53803.wifimonitor.ui

import DetailsScreen
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pl.tk53803.wifimonitor.ui.screens.HomeScreen
import pl.tk53803.wifimonitor.ui.screens.PermissionRequestScreen

@Composable
fun MainNavigation(
    onPermissionsGranted: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "permissions") {
        composable("permissions") {
            PermissionRequestScreen (onPermissionsGranted = {
                onPermissionsGranted()
                navController.navigate("main") {
                    popUpTo("permissions") { inclusive = true }
                }
            })
        }

        composable("main") {
            HomeScreen(
                onNavigateToDetail = { bssid -> navController.navigate("detail/$bssid") }
            )
        }

        composable(
            route = "detail/{bssid}",
            arguments = listOf(navArgument("bssid") { type = NavType.StringType })
        ) { backStackEntry ->
            val bssid = backStackEntry.arguments?.getString("bssid")
            if(bssid != null) {
                DetailsScreen (
                    bssid = bssid,
                    onBack = { navController.popBackStack() }
                )
            }else {
                navController.popBackStack()
            }
        }
    }
}
