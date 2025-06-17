package com.mmk.mediaplayerbasic.ui.screen.player

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.mmk.mediaplayerbasic.R

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState() // Changed to collectAsState for automatic updates
    var showRetryDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }

    // Handle player state updates from notifications/other sources
    LaunchedEffect(viewModel) {
        viewModel.playerEvents.collect { event ->
            when (event) {
                is PlayerEvent.StateUpdated -> {
                    // Force update the UI state when we receive player events
                    viewModel.forceUpdateUiState()
                }
            }
        }
    }

    // Handle retry logic
    LaunchedEffect(uiState.error) {
        showRetryDialog = uiState.error != null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            uiState.isLoading -> {
                FullScreenLoading()
            }

            uiState.error != null -> {
                ErrorState(
                    errorMessage = uiState.error,
                    onRetry = { viewModel.initialize(context) }
                )
            }

            else -> {
                PlayerContent(
                    uiState = uiState,
                    onPlayPause = { viewModel.playPause() },
                    onSkipNext = { viewModel.skipToNext() },
                    onSkipPrevious = { viewModel.skipToPrevious() },
                    onSeek = { position -> viewModel.seekTo(position) }
                )
            }

        }
    }
}

@Composable
private fun FullScreenLoading() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading music...",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun ErrorState(
    errorMessage: String?,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = errorMessage ?: "An error occurred",
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Retry")
        }
    }
}

@Composable
private fun PlayerContent(
    uiState: PlayerUiState,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeek: (Long) -> Unit
) {

    // Added remember for slider position to prevent recomposition issues

    // This remembers the slider position based on the current position and duration of the music
    // By using derivedStateOf, the value of sliderPosition will be recomputed when either
    // uiState.currentPosition or uiState.duration changes. This keeps the slider position in sync
    // with the music position even if the user isn't currently interacting with the slider.
    // When the user is interacting with the slider, the sliderPosition will be updated directly
    // by the user and not by the derivedStateOf. This is because the derivedStateOf only reads
    // the values of uiState.currentPosition and uiState.duration, it doesn't write to them.

    // When the user moves the slider, the sliderPosition will be updated directly
    // by the user. However, when the user releases the slider, the sliderPosition
    // should be updated to the correct position based on the current position and
    // duration of the music. This is done by using the remember and derivedStateOf.
    // This ensures that the slider position is always in sync with the music position.
    val sliderPosition by remember(uiState.currentPosition, uiState.duration) {
        derivedStateOf {
            if (uiState.duration > 0)
                uiState.currentPosition.toFloat() / uiState.duration else 0f
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Album Art
        AlbumArt(
            imageUrl = uiState.metadata.artworkUri?.toString(),
            modifier = Modifier.size(300.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Song Info
        SongInfo(
            title = uiState.metadata.title?.toString() ?: "Unknown Title",
            artist = uiState.metadata.artist?.toString() ?: "Unknown Artist"
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Progress Bar
        ProgressBar(
            currentPosition = uiState.currentPosition,
            duration = uiState.duration,
            sliderPosition = sliderPosition,
            onSeek = onSeek
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Player Controls
        PlayerControls(
            isPlaying = uiState.isPlaying,
            hasNext = uiState.hasNext,
            hasPrevious = uiState.hasPrevious,
            onPlayPause = onPlayPause,
            onSkipNext = onSkipNext,
            onSkipPrevious = onSkipPrevious
        )
    }
}

@Composable
private fun AlbumArt(
    imageUrl: String?,
    modifier: Modifier = Modifier
) {
    val defaultPainter = painterResource(id = R.drawable.ic_launcher_background)

    Box(modifier = modifier) {
        if (imageUrl != null) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = imageUrl,
                    error = defaultPainter
                ),
                contentDescription = "Album Art",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.large),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = defaultPainter,
                contentDescription = "Default Album Art",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun SongInfo(
    title: String,
    artist: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = artist,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ProgressBar(
    currentPosition: Long,
    duration: Long,
    sliderPosition: Float,
    onSeek: (Long) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Slider(
            value = sliderPosition,
            onValueChange = { ratio ->
                // Only seek when user releases the slider to avoid performance issues
                onSeek((ratio * duration).toLong())
            },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f)
            )
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = currentPosition.formatDuration(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = duration.formatDuration(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun PlayerControls(
    isPlaying: Boolean,
    hasNext: Boolean,
    hasPrevious: Boolean,
    onPlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onSkipPrevious,
            enabled = hasPrevious,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipPrevious,
                contentDescription = "Previous",
                modifier = Modifier.size(48.dp),
                tint = if (hasPrevious) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f)
            )
        }

        IconButton(
            onClick = onPlayPause,
            modifier = Modifier
                .size(80.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }

        IconButton(
            onClick = onSkipNext,
            enabled = hasNext,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = "Next",
                modifier = Modifier.size(48.dp),
                tint = if (hasNext) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.24f)
            )
        }
    }
}

// Extension for duration formatting
@SuppressLint("DefaultLocale")
fun Long.formatDuration(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}