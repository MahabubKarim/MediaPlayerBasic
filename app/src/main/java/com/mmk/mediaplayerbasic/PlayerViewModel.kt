package com.mmk.mediaplayerbasic

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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayerViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    var mediaController: MediaController? by mutableStateOf(null)
        private set

    private var progressUpdateJob: Job? = null

    fun initialize(context: Context) {
        if (mediaController != null) return

        val controllerFuture = MediaController.Builder(
            context,
            PlayerService.getSessionToken(context)
        ).buildAsync()

        controllerFuture.addListener(
            {
                mediaController = controllerFuture.get().apply {
                    addListener(PlayerListener())
                    prepare()
                    updateUiState()
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
        mediaController?.seekToNext()
    }

    fun skipToPrevious() {
        mediaController?.seekToPrevious()
    }

    fun seekTo(position: Long) {
        mediaController?.seekTo(position)
    }

    private fun updateUiState() {
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

    private fun startProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch {
            while (true) {
                updateUiState()
                delay(500)
            }
        }
    }

    private inner class PlayerListener : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            updateUiState()
            if (player.isPlaying) {
                startProgressUpdates()
            } else {
                progressUpdateJob?.cancel()
            }
        }
    }

    override fun onCleared() {
        mediaController?.release()
        progressUpdateJob?.cancel()
        super.onCleared()
    }
}

data class PlayerUiState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val metadata: MediaMetadata = MediaMetadata.EMPTY,
    val hasNext: Boolean = false,
    val hasPrevious: Boolean = false
)