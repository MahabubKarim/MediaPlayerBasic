package com.mmk.mediaplayerbasic

import android.app.Application
import android.util.Log
import com.mmk.mediaplayerbasic.data.local.repository.TrackRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class MediaPlayerApp : Application() {
    @Inject
    lateinit var repository: TrackRepository

    override fun onCreate() {
        super.onCreate()
        ensureInitialDataLoaded()
    }

    private fun ensureInitialDataLoaded() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tracks = repository.getAllTracks().first()
                if (tracks.isEmpty()) {
                    repository.refreshTracks(BuildConfig.JAMENDO_CLIENT_ID)
                }
            } catch (e: Exception) {
                Log.e("MediaPlayerApp", "Error loading initial data", e)
            }
        }
    }
}