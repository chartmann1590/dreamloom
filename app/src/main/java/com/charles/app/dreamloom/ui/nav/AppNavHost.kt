package com.charles.app.dreamloom.ui.nav

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Brightness2
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.charles.app.dreamloom.ui.theme.DreamColors
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.charles.app.dreamloom.feature.atlas.AtlasScreen
import com.charles.app.dreamloom.feature.detail.DreamDetailScreen
import com.charles.app.dreamloom.feature.home.HomeScreen
import com.charles.app.dreamloom.feature.insight.InsightScreen
import com.charles.app.dreamloom.feature.newdream.InterpretingScreen
import com.charles.app.dreamloom.feature.newdream.RecordingScreen
import com.charles.app.dreamloom.feature.onboarding.ModelDownloadScreen
import com.charles.app.dreamloom.feature.onboarding.OnboardingPermissionsScreen
import com.charles.app.dreamloom.feature.onboarding.OnboardingPrivacyScreen
import com.charles.app.dreamloom.feature.onboarding.SplashScreen
import com.charles.app.dreamloom.feature.onboarding.WelcomeScreen
import com.charles.app.dreamloom.feature.oracle.OracleScreen
import com.charles.app.dreamloom.feature.settings.AboutScreen
import com.charles.app.dreamloom.feature.settings.PrivacySettingsScreen
import com.charles.app.dreamloom.feature.settings.RemindersScreen
import com.charles.app.dreamloom.feature.settings.SettingsRootScreen
import com.charles.app.dreamloom.navigation.Routes
@Composable
fun AppNavHost(
    navController: NavHostController,
) {
    val navBackStack by navController.currentBackStackEntryAsState()
    val showBottom = when (navBackStack?.destination?.route) {
        Routes.HOME, Routes.ATLAS, Routes.INSIGHT, Routes.SETTINGS -> true
        else -> false
    }
    val bottomItems = listOf(
        NavItem("Home", Icons.Outlined.Brightness2, Routes.HOME),
        NavItem("Atlas", Icons.Outlined.Book, Routes.ATLAS),
        NavItem("Insight", Icons.Outlined.Explore, Routes.INSIGHT),
        NavItem("Settings", Icons.Outlined.Settings, Routes.SETTINGS),
    )
    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottom) {
                Column {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = DreamColors.InkFaint.copy(alpha = 0.15f),
                    )
                    NavigationBar(
                        containerColor = Color.Transparent,
                        tonalElevation = 0.dp,
                    ) {
                        val current = navBackStack?.destination
                        bottomItems.forEach { item ->
                            val selected = current?.hierarchy?.any { it.route == item.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = DreamColors.Moonglow,
                                    selectedTextColor = DreamColors.Moonglow,
                                    indicatorColor = DreamColors.IndigoSoft.copy(alpha = 0.4f),
                                    unselectedIconColor = DreamColors.InkMuted,
                                    unselectedTextColor = DreamColors.InkMuted,
                                ),
                            )
                        }
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.SPLASH,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            composable(Routes.SPLASH) {
                SplashScreen(
                    onDone = { dest -> navController.navigate(dest) { popUpTo(Routes.SPLASH) { inclusive = true } } },
                )
            }
            composable(Routes.WELCOME) { WelcomeScreen { navController.navigate(Routes.PRIVACY) } }
            composable(Routes.PRIVACY) { OnboardingPrivacyScreen { navController.navigate(Routes.MODEL_DOWNLOAD) } }
            composable(Routes.MODEL_DOWNLOAD) {
                ModelDownloadScreen(
                    onDone = { navController.navigate(Routes.PERMISSIONS) },
                )
            }
            composable(Routes.PERMISSIONS) {
                OnboardingPermissionsScreen(
                    onDone = { navController.navigate(Routes.HOME) { popUpTo(Routes.SPLASH) { inclusive = true } } },
                )
            }
            composable(Routes.HOME) {
                HomeScreen(
                    onNewDream = { navController.navigate(Routes.RECORDING) },
                    onOracle = { navController.navigate(Routes.ORACLE) },
                )
            }
            composable(Routes.RECORDING) {
                RecordingScreen(
                    onBack = { navController.popBackStack() },
                    onInterpreting = { id -> navController.navigate(Routes.interpreting(id)) { launchSingleTop = true } },
                )
            }
            composable(
                Routes.INTERPRETING,
                listOf(navArgument("id") { type = NavType.LongType }),
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getLong("id") ?: return@composable
                InterpretingScreen(
                    dreamId = id,
                    onComplete = { detailId -> navController.navigate(Routes.dreamDetail(detailId)) { popUpTo(Routes.HOME) } },
                )
            }
            composable(
                Routes.DREAM_DETAIL,
                listOf(navArgument("id") { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: return@composable
                DreamDetailScreen(
                    id = id,
                    onBack = { navController.popBackStack() },
                )
            }
            composable(Routes.ATLAS) {
                AtlasScreen(
                    onOpenDream = { id -> navController.navigate(Routes.dreamDetail(id)) },
                )
            }
            composable(Routes.INSIGHT) { InsightScreen() }
            composable(Routes.ORACLE) { OracleScreen { navController.popBackStack() } }
            composable(Routes.SETTINGS) {
                SettingsRootScreen(
                    onPrivacy = { navController.navigate(Routes.SETTINGS_PRIVACY) },
                    onReminders = { navController.navigate(Routes.SETTINGS_REMINDERS) },
                    onAbout = { navController.navigate(Routes.SETTINGS_ABOUT) },
                )
            }
            composable(Routes.SETTINGS_PRIVACY) { PrivacySettingsScreen { navController.popBackStack() } }
            composable(Routes.SETTINGS_REMINDERS) { RemindersScreen { navController.popBackStack() } }
            composable(Routes.SETTINGS_ABOUT) { AboutScreen { navController.popBackStack() } }
        }
    }
}

private data class NavItem(val label: String, val icon: ImageVector, val route: String)
