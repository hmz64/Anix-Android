package com.anix.rx.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.anix.rx.ui.screens.auth.LoginScreen
import com.anix.rx.ui.screens.auth.RegisterScreen
import com.anix.rx.ui.screens.browse.BrowseScreen
import com.anix.rx.ui.screens.detail.AnimeDetailScreen
import com.anix.rx.ui.screens.home.HomeScreen
import com.anix.rx.ui.screens.profile.ProfileScreen
import com.anix.rx.ui.screens.watch.WatchScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object Browse : Screen("browse")
    object Detail : Screen("detail/{animeId}") {
        fun createRoute(animeId: Long) = "detail/$animeId"
    }
    object Watch : Screen("watch/{animeId}/{episode}") {
        fun createRoute(animeId: Long, episode: Int) = "watch/$animeId/$episode"
    }
    object Profile : Screen("profile")
    object Moderator : Screen("moderator")
    object Admin : Screen("admin")
}

@Composable
fun AniXNavHost(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { 300 },
                animationSpec = tween(300, easing = androidx.compose.animation.core.EaseOut)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -300 },
                animationSpec = tween(300, easing = androidx.compose.animation.core.EaseOut)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -300 },
                animationSpec = tween(300, easing = androidx.compose.animation.core.EaseOut)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { 300 },
                animationSpec = tween(300, easing = androidx.compose.animation.core.EaseOut)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        // Auth
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = { navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }}
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = { navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Login.route) { inclusive = true }
                }}
            )
        }
        
        // Main Screens
        composable(Screen.Home.route) {
            HomeScreen(
                onAnimeClick = { animeId ->
                    navController.navigate(Screen.Detail.createRoute(animeId))
                },
                onProfileClick = { navController.navigate(Screen.Profile.route) },
                onModeratorClick = { navController.navigate(Screen.Moderator.route) },
                onAdminClick = { navController.navigate(Screen.Admin.route) }
            )
        }
        
        composable(Screen.Browse.route) {
            BrowseScreen(
                onAnimeClick = { animeId ->
                    navController.navigate(Screen.Detail.createRoute(animeId))
                }
            )
        }
        
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("animeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val animeId = backStackEntry.arguments?.getLong("animeId") ?: 0L
            AnimeDetailScreen(
                animeId = animeId,
                onBackClick = { navController.popBackStack() },
                onWatchEpisode = { epNum ->
                    navController.navigate(Screen.Watch.createRoute(animeId, epNum))
                }
            )
        }
        
        composable(
            route = Screen.Watch.route,
            arguments = listOf(
                navArgument("animeId") { type = NavType.LongType },
                navArgument("episode") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val animeId = backStackEntry.arguments?.getLong("animeId") ?: 0L
            val episode = backStackEntry.arguments?.getInt("episode") ?: 1
            WatchScreen(
                animeId = animeId,
                initialEpisode = episode,
                onBackClick = { navController.popBackStack() },
                onSelectEpisode = { epNum ->
                    navController.navigate(Screen.Watch.createRoute(animeId, epNum)) {
                        popUpTo(Screen.Watch.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Moderator.route) {
            // ModeratorScreen()
        }
        
        composable(Screen.Admin.route) {
            // AdminScreen()
        }
    }
}