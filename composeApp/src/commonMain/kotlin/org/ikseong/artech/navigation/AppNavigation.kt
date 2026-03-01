package org.ikseong.devnews.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.ikseong.devnews.ui.screen.detail.DetailScreen
import org.ikseong.devnews.ui.screen.favorite.FavoriteScreen
import org.ikseong.devnews.ui.screen.history.HistoryScreen
import org.ikseong.devnews.ui.screen.home.HomeScreen
import org.ikseong.devnews.ui.screen.settings.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val isDetailScreen = currentDestination?.hasRoute(Route.Detail::class) == true

    val navigateToHome: () -> Unit = {
        navController.navigate(Route.Home) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        bottomBar = {
            if (!isDetailScreen) {
                NavigationBar {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.hasRoute(destination.route::class)
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
                                    contentDescription = destination.label,
                                )
                            },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Home,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable<Route.Home> {
                HomeScreen(
                    onArticleClick = { articleId, link ->
                        navController.navigate(Route.Detail(articleId = articleId, link = link))
                    },
                )
            }
            composable<Route.Favorite> {
                FavoriteScreen(
                    onArticleClick = { articleId, link ->
                        navController.navigate(Route.Detail(articleId = articleId, link = link))
                    },
                    onNavigateToHome = navigateToHome,
                )
            }
            composable<Route.History> {
                HistoryScreen(
                    onArticleClick = { articleId, link ->
                        navController.navigate(Route.Detail(articleId = articleId, link = link))
                    },
                    onNavigateToHome = navigateToHome,
                )
            }
            composable<Route.Settings> {
                SettingsScreen()
            }
            composable<Route.Detail> {
                DetailScreen(
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
