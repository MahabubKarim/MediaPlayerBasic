package com.mmk.mediaplayerbasic.ui.screen.player

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import com.google.common.util.concurrent.MoreExecutors
import com.mmk.mediaplayerbasic.service.PlayerService
import com.mmk.mediaplayerbasic.data.local.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PlayerEvent {
    object StateUpdated : PlayerEvent()
}

data class PlayerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val metadata: MediaMetadata = MediaMetadata.EMPTY,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false
)

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repository: TrackRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    val playerEvents = MutableSharedFlow<PlayerEvent>()

    var mediaController: MediaController? by mutableStateOf(null)
        private set

    var progressUpdateJob: Job? = null

    fun initialize(context: Context) {
        if (mediaController != null) return

        // Clear previous controller if exists
        mediaController?.release()

        _uiState.update { it.copy(isLoading = true) }

        // Check if we have data first
        viewModelScope.launch {
            try {
                val hasData = repository.getAllTracks().first().isNotEmpty()
                if (!hasData) {
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = "No tracks available. Please check your connection."
                    )}
                    return@launch
                }

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
    }
    fun playPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) controller.pause() else controller.play()
        }
    }

    fun skipToNext() {
        try {
            mediaController?.seekToNext()
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to skip: ${e.message}") }
        }
    }

    fun skipToPrevious() {
        try {
            mediaController?.seekToPrevious()
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to skip: ${e.message}") }
        }
    }

    fun seekTo(position: Long) {
        try {
            mediaController?.seekTo(position)
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to seek: ${e.message}") }
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

    fun forceUpdateUiState() {
        updateUiState()

        // Additional handling based on player state if needed
        mediaController?.let { it ->
            if (it.isPlaying) {
                startProgressUpdates()
            } else {
                progressUpdateJob?.cancel()
            }
        }
    }

    fun startProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch {
            while (true) {
                updateUiState()
                delay(150)
            }
        }
    }

    override fun onCleared() {
        mediaController?.release()
        progressUpdateJob?.cancel()
        super.onCleared()
    }

    private inner class PlayerListener : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            viewModelScope.launch {
                playerEvents.emit(PlayerEvent.StateUpdated)
            }
        }
    }
}