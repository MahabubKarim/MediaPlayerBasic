package com.mmk.mediaplayerbasic.ui.screen.navigation

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mmk.mediaplayerbasic.service.PlayerService
import com.mmk.mediaplayerbasic.ui.screen.home.HomeScreen
import com.mmk.mediaplayerbasic.ui.screen.home.HomeViewModel
import com.mmk.mediaplayerbasic.ui.screen.player.PlayerScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavGraph(
    navController: NavHostController
) {
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = "home") {
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