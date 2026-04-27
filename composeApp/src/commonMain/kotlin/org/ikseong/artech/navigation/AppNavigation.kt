package org.ikseong.artech.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import org.ikseong.artech.analytics.AnalyticsEvents
import org.ikseong.artech.analytics.AnalyticsTracker
import org.ikseong.artech.ui.screen.blog.BlogScreen
import org.ikseong.artech.ui.screen.bloglist.BlogListScreen
import org.ikseong.artech.ui.screen.detail.DetailScreen
import org.ikseong.artech.ui.screen.favorite.FavoriteScreen
import org.ikseong.artech.ui.screen.history.HistoryScreen
import org.ikseong.artech.ui.screen.home.HomeScreen
import org.ikseong.artech.ui.screen.contact.ContactScreen
import org.ikseong.artech.ui.screen.latest.LatestFeedScreen
import org.ikseong.artech.ui.screen.settings.SettingsScreen
import org.koin.compose.koinInject

@Composable
fun AppNavigation(
    analyticsTracker: AnalyticsTracker = koinInject(),
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val currentScreenName = currentDestination.analyticsScreenName()

    LaunchedEffect(currentScreenName) {
        currentScreenName?.let { screenName ->
            analyticsTracker.logEvent(AnalyticsEvents.screenView(screenName))
        }
    }

    val isDetailScreen = currentDestination?.hasRoute(Route.Detail::class) == true
    val isBlogScreen = currentDestination?.hasRoute(Route.Blog::class) == true
    val isBlogListScreen = currentDestination?.hasRoute(Route.BlogList::class) == true
    val isContactScreen = currentDestination?.hasRoute(Route.Contact::class) == true
    val isLatestFeedScreen = currentDestination?.hasRoute(Route.LatestFeed::class) == true

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
            if (!isDetailScreen && !isBlogScreen && !isBlogListScreen && !isContactScreen && !isLatestFeedScreen) {
                val primaryColor = MaterialTheme.colorScheme.primary

                Column {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                    )
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 0.dp,
                    ) {
                    TopLevelDestination.entries.forEach { destination ->
                        val selected = currentDestination?.hierarchy?.any {
                            it.hasRoute(destination.route::class)
                        } == true

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                analyticsTracker.logEvent(AnalyticsEvents.tabSelect(destination.analyticsName))
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
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = primaryColor,
                                selectedTextColor = primaryColor,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = Color.Transparent,
                            ),
                        )
                    }
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
                    onBlogClick = { blogSource ->
                        navController.navigate(Route.Blog(blogSource = blogSource))
                    },
                    onBlogListClick = {
                        navController.navigate(Route.BlogList)
                    },
                    onLatestFeedClick = {
                        analyticsTracker.logEvent(AnalyticsEvents.latestFeedOpen(source = "home"))
                        navController.navigate(Route.LatestFeed())
                    },
                    onTopicClick = { category ->
                        analyticsTracker.logEvent(
                            AnalyticsEvents.categorySelect(
                                source = "home_topic_hub",
                                category = category,
                            ),
                        )
                        navController.navigate(Route.LatestFeed(initialCategory = category))
                    },
                )
            }
            composable<Route.LatestFeed> {
                LatestFeedScreen(
                    onArticleClick = { articleId, link ->
                        navController.navigate(Route.Detail(articleId = articleId, link = link))
                    },
                    onBlogClick = { blogSource ->
                        navController.navigate(Route.Blog(blogSource = blogSource))
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable<Route.Favorite> {
                FavoriteScreen(
                    onArticleClick = { articleId, link ->
                        analyticsTracker.logEvent(
                            AnalyticsEvents.articleOpen(
                                articleId = articleId,
                                source = "favorite",
                            ),
                        )
                        navController.navigate(Route.Detail(articleId = articleId, link = link))
                    },
                    onBlogClick = { blogSource ->
                        analyticsTracker.logEvent(
                            AnalyticsEvents.blogOpen(
                                source = "favorite",
                                blogSource = blogSource,
                            ),
                        )
                        navController.navigate(Route.Blog(blogSource = blogSource))
                    },
                    onNavigateToHome = navigateToHome,
                )
            }
            composable<Route.History> {
                HistoryScreen(
                    onArticleClick = { articleId, link ->
                        analyticsTracker.logEvent(
                            AnalyticsEvents.articleOpen(
                                articleId = articleId,
                                source = "history",
                            ),
                        )
                        navController.navigate(Route.Detail(articleId = articleId, link = link))
                    },
                    onBlogClick = { blogSource ->
                        analyticsTracker.logEvent(
                            AnalyticsEvents.blogOpen(
                                source = "history",
                                blogSource = blogSource,
                            ),
                        )
                        navController.navigate(Route.Blog(blogSource = blogSource))
                    },
                    onNavigateToHome = navigateToHome,
                )
            }
            composable<Route.Settings> {
                SettingsScreen(
                    onBlogListClick = {
                        navController.navigate(Route.BlogList)
                    },
                    onContactClick = {
                        navController.navigate(Route.Contact)
                    },
                )
            }
            composable<Route.Contact> {
                ContactScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable<Route.Detail> {
                DetailScreen(
                    onBack = { navController.popBackStack() },
                    onBlogClick = { blogSource ->
                        analyticsTracker.logEvent(
                            AnalyticsEvents.blogOpen(
                                source = "detail",
                                blogSource = blogSource,
                            ),
                        )
                        navController.navigate(Route.Blog(blogSource = blogSource))
                    },
                )
            }
            composable<Route.BlogList> {
                BlogListScreen(
                    onBlogClick = { blogSource ->
                        analyticsTracker.logEvent(
                            AnalyticsEvents.blogOpen(
                                source = "blog_list",
                                blogSource = blogSource,
                            ),
                        )
                        navController.navigate(Route.Blog(blogSource = blogSource))
                    },
                    onBack = { navController.popBackStack() },
                )
            }
            composable<Route.Blog> {
                BlogScreen(
                    onArticleClick = { articleId, link ->
                        analyticsTracker.logEvent(
                            AnalyticsEvents.articleOpen(
                                articleId = articleId,
                                source = "blog",
                            ),
                        )
                        navController.navigate(Route.Detail(articleId = articleId, link = link))
                    },
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}

private val TopLevelDestination.analyticsName: String
    get() = when (this) {
        TopLevelDestination.HOME -> "home"
        TopLevelDestination.FAVORITE -> "favorite"
        TopLevelDestination.HISTORY -> "history"
        TopLevelDestination.SETTINGS -> "settings"
    }

private fun NavDestination?.analyticsScreenName(): String? =
    when {
        this == null -> null
        hasRoute(Route.Home::class) -> "home"
        hasRoute(Route.LatestFeed::class) -> "latest_feed"
        hasRoute(Route.Favorite::class) -> "favorite"
        hasRoute(Route.History::class) -> "history"
        hasRoute(Route.Settings::class) -> "settings"
        hasRoute(Route.Contact::class) -> "contact"
        hasRoute(Route.Detail::class) -> "detail"
        hasRoute(Route.BlogList::class) -> "blog_list"
        hasRoute(Route.Blog::class) -> "blog"
        else -> null
    }
