package com.yep.app.ui.navigation

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import com.yep.app.ui.camera.CameraScreen
import com.yep.app.ui.history.HistoryScreen
import com.yep.app.ui.onboarding.OnboardingScreen
import com.yep.app.ui.onboarding.OnboardingViewModel
import com.yep.app.ui.photo.PhotoViewerScreen
import com.yep.app.ui.streaks.StreaksScreen
import com.yep.app.ui.today.TodayScreen
import com.yep.app.ui.today.TodayViewModel
import com.yep.app.ui.theme.GreenPrimary
import com.yep.app.ui.theme.NeutralGray

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Today : Screen("today", "Today", Icons.Filled.CheckCircle)
    object History : Screen("history", "History", Icons.Filled.DateRange)
    object Streaks : Screen("streaks", "Streaks", Icons.Filled.Star)
}

val bottomNavItems = listOf(Screen.Today, Screen.History, Screen.Streaks)

private const val CAMERA_ROUTE = "camera/{itemId}/{itemLabel}"
private const val PHOTO_ROUTE = "photo?photoPath={photoPath}&itemLabel={itemLabel}&confirmedAt={confirmedAt}"

@Composable
fun YepNavigation() {
    val onboardingVm: OnboardingViewModel = hiltViewModel()
    val onboardingComplete by onboardingVm.onboardingComplete.collectAsState()

    when (onboardingComplete) {
        null  -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        false -> OnboardingScreen(viewModel = onboardingVm)
        true  -> YepApp()
    }
}

@Composable
private fun YepApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute?.startsWith("camera/") != true
            && currentRoute?.startsWith("photo") != true

    val todayEntry = runCatching { navController.getBackStackEntry(Screen.Today.route) }.getOrNull()
    val todayVm: TodayViewModel? = todayEntry?.let { hiltViewModel(it) }
    val isTodayEditMode = todayVm?.isEditMode?.collectAsState()?.value ?: false

    Scaffold(
        bottomBar = {
                val currentDestination = navBackStackEntry?.destination
                NavigationBar(
                    modifier = Modifier.graphicsLayer { alpha = if (showBottomBar) 1f else 0f }
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (isTodayEditMode && screen.route != Screen.Today.route) {
                                    todayVm?.requestShakeHeader()
                                } else {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.label
                                )
                            },
                            label = { Text(screen.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = GreenPrimary,
                                selectedTextColor = GreenPrimary,
                                unselectedIconColor = NeutralGray,
                                unselectedTextColor = NeutralGray,
                                indicatorColor = GreenPrimary.copy(alpha = 0.1f)
                            )
                        )
                    }
                }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Today.route,
            modifier = Modifier.padding(paddingValues).consumeWindowInsets(paddingValues)
        ) {
            composable(Screen.Today.route) {
                TodayScreen(
                    onNavigateToCamera = { itemId, itemLabel ->
                        navController.navigate("camera/$itemId/${Uri.encode(itemLabel)}")
                    },
                    onNavigateToPhoto = { photoPath, itemLabel, confirmedAt ->
                        navController.navigate(
                            "photo?photoPath=${Uri.encode(photoPath)}" +
                                    "&itemLabel=${Uri.encode(itemLabel)}" +
                                    "&confirmedAt=$confirmedAt"
                        )
                    }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    onNavigateToPhoto = { photoPath, itemLabel, confirmedAt ->
                        navController.navigate(
                            "photo?photoPath=${Uri.encode(photoPath)}" +
                                    "&itemLabel=${Uri.encode(itemLabel)}" +
                                    "&confirmedAt=$confirmedAt"
                        )
                    }
                )
            }
            composable(Screen.Streaks.route) { StreaksScreen() }
            composable(
                route = CAMERA_ROUTE,
                arguments = listOf(
                    navArgument("itemId") { type = NavType.StringType },
                    navArgument("itemLabel") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val itemId = backStackEntry.arguments?.getString("itemId") ?: return@composable
                val itemLabel = backStackEntry.arguments?.getString("itemLabel") ?: return@composable
                CameraScreen(
                    itemId = itemId,
                    itemLabel = itemLabel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable(
                route = PHOTO_ROUTE,
                arguments = listOf(
                    navArgument("photoPath") { type = NavType.StringType },
                    navArgument("itemLabel") { type = NavType.StringType },
                    navArgument("confirmedAt") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val photoPath = backStackEntry.arguments?.getString("photoPath")
                    ?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.name()) }
                    ?: return@composable
                val itemLabel = backStackEntry.arguments?.getString("itemLabel")
                    ?.let { URLDecoder.decode(it, StandardCharsets.UTF_8.name()) }
                    ?: return@composable
                val confirmedAt = backStackEntry.arguments?.getLong("confirmedAt") ?: 0L
                PhotoViewerScreen(
                    photoPath = photoPath,
                    itemLabel = itemLabel,
                    confirmedAt = confirmedAt,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
