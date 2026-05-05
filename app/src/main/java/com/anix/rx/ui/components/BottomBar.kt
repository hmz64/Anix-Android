package com.anix.rx.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.anix.rx.ui.navigation.Screen

data class BottomNavItem(
    val route: String,
    val icon: @Composable () -> Unit,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, { Icon(Icons.Default.Home, contentDescription = "Home") }, "Home"),
    BottomNavItem(Screen.Browse.route, { Icon(Icons.Default.Search, contentDescription = "Browse") }, "Browse"),
    BottomNavItem("watchlist", { Icon(Icons.Default.Bookmark, contentDescription = "Watchlist") }, "Watchlist"),
    BottomNavItem(Screen.Profile.route, { Icon(Icons.Default.Person, contentDescription = "Profile") }, "Profile")
)

@Composable
fun AniXBottomBar(
    navController: NavController,
    visible: Boolean = true
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        NavigationBar(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
            val navBackStackEntry = navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry.value?.destination?.route

            bottomNavItems.forEach { item ->
                NavigationBarItem(
                    selected = currentRoute == item.route,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = item.icon,
                    label = { Text(item.label) },
                    alwaysShowLabel = false
                )
            }
        }
    }
}
