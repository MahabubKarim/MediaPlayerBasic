package com.mmk.mediaplayerbasic.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.mmk.mediaplayerbasic.data.local.entity.TrackEntity
import com.mmk.mediaplayerbasic.data.local.repository.TrackRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class PlayerService : MediaSessionService() {
    @Inject
    lateinit var repository: TrackRepository
    private var mediaSession: MediaSession? = null
    // private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
    }

    private fun initializePlayer() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tracks = repository.getAllTracks().first()
                if (tracks.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        initializePlayerWithTracks(tracks)
                    }
                } else {
                    Log.w("PlayerService", "No tracks available")
                    stopSelf()
                }
            } catch (e: Exception) {
                Log.e("PlayerService", "Error initializing player", e)
                stopSelf()
            }
        }
    }

    private fun initializePlayerWithTracks(tracks: List<TrackEntity>) {
        // Run on main thread as ExoPlayer needs main thread
        val player = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                setMediaItems(tracks.map { it.toMediaItem() })
                prepare()
            }

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(PlayerSessionCallback())
            .build()
    }

    private fun TrackEntity.toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId(id)
            .setUri(audioUrl)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setArtist(artist)
                    .setArtworkUri(imageUrl.toUri())
                    .setAlbumTitle("Jamendo")
                    .build()
            )
            .build()
    }

    private fun MediaItem.toTrackEntity(): TrackEntity {
        return TrackEntity(
            id = mediaId,
            title = mediaMetadata.title.toString(),
            artist = mediaMetadata.artist.toString(),
            imageUrl = mediaMetadata.artworkUri.toString(),
            duration = 0,
            audioUrl = "",
            lastUpdated = 0
        )
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player != null && !player.isPlaying) {
            stopSelf()
        }
    }

    inner class PlayerSessionCallback : MediaSession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            val updatedMediaItems = mediaItems.map { item ->
                item.buildUpon()
                    .setUri(item.mediaId) // Use mediaId as URI
                    .build()
            }.toMutableList()
            return Futures.immediateFuture(updatedMediaItems)
        }
    }

    companion object {
        fun getSessionToken(context: Context) = SessionToken(
            context,
            ComponentName(context, PlayerService::class.java)
        )
    }
}