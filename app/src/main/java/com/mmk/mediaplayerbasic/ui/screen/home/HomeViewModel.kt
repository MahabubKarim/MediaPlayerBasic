package com.mmk.mediaplayerbasic.ui.screen.home

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.common.util.concurrent.MoreExecutors
import com.mmk.mediaplayerbasic.data.local.entity.TrackEntity
import com.mmk.mediaplayerbasic.data.local.paging.TrackPagingSource
import com.mmk.mediaplayerbasic.data.local.repository.TrackRepository
import com.mmk.mediaplayerbasic.service.PlayerService
import com.mmk.mediaplayerbasic.ui.screen.player.PlayerEvent
import com.mmk.mediaplayerbasic.ui.screen.player.PlayerUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: TrackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    val playerEvents = MutableSharedFlow<PlayerEvent>()
    private var mediaController: MediaController? = null

    var currentTracks: List<TrackEntity> = emptyList()

    val tracks = Pager(
        config = PagingConfig(pageSize = 20),
        pagingSourceFactory = { TrackPagingSource(repository) }
    ).flow.cachedIn(viewModelScope)

    fun initialize(context: Context) {
        if (mediaController != null) return

        // Clear previous controller if exists
        mediaController?.release()

        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            repository.getAllTracks().collect { trackList ->
                currentTracks = trackList
            }
        }

        // Check if we have data first
        viewModelScope.launch {
            try {
                val hasData : PagingData<TrackEntity> = repository.getPagedTracks().first()
                if (hasData == null) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "No tracks available. Please check your connection."
                    )}
                    return@launch
                }
                /**/
                // Only proceed if we have data
                initializeMediaController(context)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = true,
                    error = "Error loading tracks: ${e.localizedMessage}"
                )}
            }
        }
    }

    private fun initializeMediaController(context: Context) {
        try {
            val controllerFuture = MediaController.Builder(
                context,
                PlayerService.Companion.getSessionToken(context)
            ).buildAsync()

            controllerFuture.addListener(
                {
                    try {
                        mediaController = controllerFuture.get().apply {
                            addListener(PlayerListener())
                            prepare()
                            updateUiState()
                        }
                        _uiState.update { it.copy(isLoading = false) }
                    } catch (e: Exception) {
                        _uiState.update { it.copy(
                            isLoading = false,
                            error = "Failed to connect to player"
                        )}
                    }
                },
                MoreExecutors.directExecutor()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private inner class PlayerListener : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            viewModelScope.launch {
                playerEvents.emit(PlayerEvent.StateUpdated)
            }
        }
    }
    fun updateUiState() {
        mediaController?.let { controller ->
            _uiState.update {
                it.copy(
                    isPlaying = controller.isPlaying,
                    currentPosition = controller.currentPosition,
                    duration = controller.duration,
                    metadata = controller.mediaMetadata,
                    hasNext = controller.hasNextMediaItem(),
                    hasPrevious = controller.hasPreviousMediaItem()
                )
            }
        }
    }


    fun playTrack(track: TrackEntity) {
        viewModelScope.launch {
            try {
                mediaController?.let { controller ->
                    // Convert TrackEntity to MediaItem
                    val mediaItem = MediaItem.Builder()
                        .setMediaId(track.id)
                        .setUri(track.audioUrl)
                        .setMediaMetadata(
                            MediaMetadata.Builder()
                                .setTitle(track.title)
                                .setArtist(track.artist)
                                .setArtworkUri(track.imageUrl.toUri())
                                .build()
                        )
                        .build()
                    // Stop current playback and prepare new track
                    controller.stop()
                    controller.setMediaItem(mediaItem)
                    controller.prepare()
                    controller.play()

                    // If you want to add to queue instead of replacing:
                    // controller.addMediaItem(mediaItem)
                } ?: throw IllegalStateException("MediaController not initialized")
            } catch (e: Exception) {
                // Handle error (e.g., show error message)
                e.printStackTrace()
            }
        }
    }

    fun playPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) controller.pause() else controller.play()
        }
    }

    override fun onCleared() {
        mediaController?.release()
        super.onCleared()
    }
}