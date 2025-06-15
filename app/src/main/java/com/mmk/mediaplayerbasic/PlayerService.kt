package com.mmk.mediaplayerbasic

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class PlayerService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                repeatMode = Player.REPEAT_MODE_ALL
                setMediaItems(buildMediaItems())
                prepare()
            }

        mediaSession = MediaSession.Builder(this, player)
            .setCallback(PlayerSessionCallback())
            .build()
    }

    private fun buildMediaItems(): List<MediaItem> {
        return listOf(
            MediaItem.Builder()
                .setUri("http://www.mp3haat.net/mp3/bengali/moner-ekla-ghore-arfin-rumey.mp3")
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle("Song 1")
                        .setArtist("Artist 1")
                        //.setArtworkUri("https://example.com/art1.jpg")
                        .build()
                )
                .build(),
            MediaItem.Builder()
                .setUri("http://www.mp3haat.net/mp3/bengali/moner-ekla-ghore-arfin-rumey.mp3")
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle("Song 2")
                        .setArtist("Artist 2")
                       // .setArtworkUri("https://example.com/art2.jpg")
                        .build()
                )
                .build(),
            MediaItem.Builder()
                .setUri("http://www.mp3haat.net/mp3/bengali/moner-ekla-ghore-arfin-rumey.mp3")
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle("Song 3")
                        .setArtist("Artist 3")
                        //.setArtworkUri("https://example.com/art3.jpg")
                        .build()
                )
                .build()
        )
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

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
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