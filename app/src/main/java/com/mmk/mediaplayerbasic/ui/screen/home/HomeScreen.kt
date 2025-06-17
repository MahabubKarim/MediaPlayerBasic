package com.mmk.mediaplayerbasic.ui.screen.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.rememberAsyncImagePainter
import com.mmk.mediaplayerbasic.data.local.entity.TrackEntity

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onMiniPlayerClick: () -> Unit
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsState()
    val tracks = viewModel.currentTracks
    val lazyPagingItems = viewModel.tracks.collectAsLazyPagingItems()

    LaunchedEffect(Unit) {
        viewModel.initialize(context)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when {
            uiState.isLoading -> FullScreenLoading()
            uiState.error != null -> ErrorState(
                errorMessage = uiState.error,
                onRetry = {
                    viewModel.initialize(context)
                }
            )

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(8.dp)
                        ) {
                            items(tracks) { track ->
                                track.let {
                                    TrackItem(
                                        track = it,
                                        onClick = {
                                            viewModel.playTrack(track)
                                        }
                                    )
                                }
                            }

                            /*lazyPagingItems.apply {
                            when {
                                loadState.refresh is LoadState.Loading -> {
                                    item { FullScreenLoading() }
                                }
                                loadState.append is LoadState.Loading -> {
                                    item { LoadingItem() }
                                }
                                loadState.refresh is LoadState.Error -> {
                                    item {
                                        ErrorState(
                                            errorMessage = uiState.error,
                                            onRetry = {
                                                viewModel.initialize(context)
                                            }
                                        )
                                    }
                                }
                                loadState.append is LoadState.Error -> {
                                    item {
                                        ErrorState(
                                            errorMessage = uiState.error,
                                            onRetry = {
                                                viewModel.initialize(context)
                                            }
                                        )
                                    }
                                }
                            }
                        }*/
                        }
                    }
                    MiniPlayer(viewModel = viewModel, onClick = onMiniPlayerClick)
                }
            }

        }
    }
}

@Composable
private fun LoadingItem() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun TrackItem(track: TrackEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = track.imageUrl),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(track.title, style = MaterialTheme.typography.bodyLarge)
                Text(track.artist, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun MiniPlayer(
    viewModel: HomeViewModel,
    onClick: () -> Unit
) {
    // Get current playing track from ViewModel
    val currentTrack by remember { mutableStateOf<TrackEntity?>(null) }

    if (currentTrack != null) {
        Surface(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = rememberAsyncImagePainter(model = currentTrack!!.imageUrl),
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(currentTrack!!.title, maxLines = 1)
                    Text(currentTrack!!.artist, maxLines = 1)
                }
                IconButton(onClick = { viewModel.playPause() }) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                }
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