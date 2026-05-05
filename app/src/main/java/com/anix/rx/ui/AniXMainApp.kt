package com.anix.rx.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.anix.rx.DeepLinkData
import com.anix.rx.ui.components.FloatingBottomNav
import com.anix.rx.ui.navigation.AniXNavHost
import com.anix.rx.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AniXApp(initialDeepLink: DeepLinkData? = null) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val mainRoutes = listOf(
        Screen.Home.route,
        Screen.Browse.route,
        Screen.Profile.route,
    )
    val showBottomNav = currentRoute in mainRoutes
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle deep link navigation once NavHost is ready
    LaunchedEffect(initialDeepLink) {
        if (initialDeepLink != null) {
            // Give NavHost time to settle on startDestination
            kotlinx.coroutines.delay(300)
            when {
                initialDeepLink.episodeNumber != null && initialDeepLink.animeId != null -> {
                    navController.navigate(
                        Screen.Watch.createRoute(initialDeepLink.animeId, initialDeepLink.episodeNumber)
                    )
                }
                initialDeepLink.animeId != null -> {
                    navController.navigate(Screen.Detail.createRoute(initialDeepLink.animeId))
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomNav,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                FloatingBottomNav(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            AniXNavHost(
                navController = navController,
                startDestination = Screen.Login.route,
                snackbarHostState = snackbarHostState
            )
        }
    }
}
