package com.mmk.mediaplayerbasic.ui.screen.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mmk.mediaplayerbasic.ui.screen.home.HomeScreen
import com.mmk.mediaplayerbasic.ui.screen.home.HomeViewModel
import com.mmk.mediaplayerbasic.ui.screen.player.PlayerScreen

@Composable
fun NavGraph(
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination = "player") {
        composable("home") {
            val viewModel = hiltViewModel<HomeViewModel>()
            HomeScreen(
                viewModel = viewModel,
                onMiniPlayerClick = {
                    navController.navigate("player")
                }
            )
        }
        composable("player") {
            PlayerScreen()
        }
    }
}