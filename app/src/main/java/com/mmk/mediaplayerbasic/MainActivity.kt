package com.mmk.mediaplayerbasic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.mmk.mediaplayerbasic.ui.theme.MediaPlayerBasicTheme

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