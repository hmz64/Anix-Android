package com.anix.rx.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.anix.rx.ui.components.LoadingIndicator
import com.anix.rx.ui.components.FloatingBottomNav
import com.anix.rx.ui.navigation.AniXNavHost
import com.anix.rx.ui.navigation.Screen
import com.anix.rx.ui.screens.auth.LoginScreen
import com.anix.rx.ui.screens.auth.RegisterScreen
import com.anix.rx.ui.screens.browse.BrowseScreen
import com.anix.rx.ui.screens.detail.AnimeDetailScreen
import com.anix.rx.ui.screens.home.HomeScreen
import com.anix.rx.ui.screens.profile.ProfileScreen
import com.anix.rx.ui.screens.watch.WatchScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AniXApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    
    val mainRoutes = listOf(
        Screen.Home.route,
        Screen.Browse.route,
        "favorites",
        Screen.Profile.route
    )
    
    val showBottomNav = currentRoute in mainRoutes
    
    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomNav,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                FloatingBottomNav(
                    currentRoute = currentRoute ?: "",
                    onNavigate = { route ->
                        when (route) {
                            "home" -> navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            }
                            "browse" -> navController.navigate(Screen.Browse.route) {
                                popUpTo(Screen.Browse.route) { inclusive = true }
                            }
                            "favorites" -> {}
                            "profile" -> navController.navigate(Screen.Profile.route)
                        }
                    }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AniXNavHost(
                navController = navController,
                startDestination = Screen.Login.route
            )
        }
    }
}