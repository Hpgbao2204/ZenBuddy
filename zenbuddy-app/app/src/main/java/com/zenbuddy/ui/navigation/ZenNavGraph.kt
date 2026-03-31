package com.zenbuddy.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.zenbuddy.ui.feature.auth.AuthRoute
import com.zenbuddy.ui.feature.breathing.BreathingScreen
import com.zenbuddy.ui.feature.chat.ChatRoute
import com.zenbuddy.ui.feature.home.HomeRoute
import com.zenbuddy.ui.feature.insights.InsightsRoute
import com.zenbuddy.ui.feature.journal.JournalRoute
import com.zenbuddy.ui.feature.mood.MoodRoute
import com.zenbuddy.ui.feature.onboarding.OnboardingScreen
import com.zenbuddy.ui.feature.quest.QuestRoute
import com.zenbuddy.ui.feature.settings.SettingsScreen

data class BottomNavItem(
    val route: Route,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Route.Home, "Home", Icons.Default.Home),
    BottomNavItem(Route.Mood, "Mood", Icons.Default.EmojiEmotions),
    BottomNavItem(Route.Journal, "Journal", Icons.AutoMirrored.Filled.MenuBook),
    BottomNavItem(Route.Chat, "Chat", Icons.AutoMirrored.Filled.Chat),
    BottomNavItem(Route.Quests, "Quests", Icons.Default.Star)
)

// Routes that should NOT show the bottom nav bar
private val noBottomBarRoutes = setOf(
    Route.Auth.path,
    Route.Breathing.path,
    Route.Insights.path,
    Route.Settings.path,
    Route.Onboarding.path
)

@Composable
fun ZenNavGraph(
    navController: NavHostController,
    startDestination: String = Route.Home.path,
    onOnboardingComplete: () -> Unit = {}
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute !in noBottomBarRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            selected = currentRoute == item.route.path,
                            onClick = {
                                navController.navigate(item.route.path) {
                                    popUpTo(Route.Home.path) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(Route.Auth.path) {
                AuthRoute(
                    onAuthSuccess = {
                        navController.navigate(Route.Onboarding.path) {
                            popUpTo(Route.Auth.path) { inclusive = true }
                        }
                    }
                )
            }
            composable(Route.Onboarding.path) {
                OnboardingScreen(
                    onFinished = {
                        onOnboardingComplete()
                        navController.navigate(Route.Home.path) {
                            popUpTo(Route.Onboarding.path) { inclusive = true }
                        }
                    }
                )
            }
            composable(Route.Home.path) {
                HomeRoute(
                    onNavigateToMood = { navController.navigate(Route.Mood.path) },
                    onNavigateToChat = { navController.navigate(Route.Chat.path) },
                    onNavigateToQuests = { navController.navigate(Route.Quests.path) },
                    onNavigateToBreathing = { navController.navigate(Route.Breathing.path) },
                    onNavigateToInsights = { navController.navigate(Route.Insights.path) },
                    onNavigateToSettings = { navController.navigate(Route.Settings.path) }
                )
            }
            composable(Route.Mood.path) {
                MoodRoute(onNavigateBack = { navController.popBackStack() })
            }
            composable(Route.Journal.path) {
                JournalRoute()
            }
            composable(Route.Chat.path) {
                ChatRoute()
            }
            composable(Route.Quests.path) {
                QuestRoute()
            }
            composable(Route.Breathing.path) {
                BreathingScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Route.Insights.path) {
                InsightsRoute(onNavigateBack = { navController.popBackStack() })
            }
            composable(Route.Settings.path) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Route.Auth.path) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
