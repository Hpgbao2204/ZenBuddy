package com.zenbuddy.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.zenbuddy.ui.feature.chat.ChatRoute
import com.zenbuddy.ui.feature.home.HomeRoute
import com.zenbuddy.ui.feature.journal.JournalRoute
import com.zenbuddy.ui.feature.mood.MoodRoute
import com.zenbuddy.ui.feature.quest.QuestRoute

data class BottomNavItem(
    val route: Route,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Route.Home, "Home", Icons.Default.Home),
    BottomNavItem(Route.Mood, "Mood", Icons.Default.EmojiEmotions),
    BottomNavItem(Route.Journal, "Journal", Icons.Default.MenuBook),
    BottomNavItem(Route.Chat, "Chat", Icons.Default.Chat),
    BottomNavItem(Route.Quests, "Quests", Icons.Default.Star)
)

@Composable
fun ZenNavGraph(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
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
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Route.Home.path,
            modifier = Modifier.padding(padding)
        ) {
            composable(Route.Home.path) {
                HomeRoute(
                    onNavigateToMood = { navController.navigate(Route.Mood.path) },
                    onNavigateToChat = { navController.navigate(Route.Chat.path) },
                    onNavigateToQuests = { navController.navigate(Route.Quests.path) }
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
        }
    }
}
