package com.mmk.mediaplayerbasic.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mmk.mediaplayerbasic.ui.screen.player.PlayerScreen
import com.mmk.mediaplayerbasic.ui.theme.MediaPlayerBasicTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MediaPlayerBasicTheme {
                PlayerScreen()
            }
        }
    }
}