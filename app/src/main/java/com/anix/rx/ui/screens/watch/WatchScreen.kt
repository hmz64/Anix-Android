package com.anix.rx.ui.screens.watch

import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.anix.rx.ui.components.*
import com.anix.rx.ui.theme.Primary
import com.anix.rx.ui.viewmodel.WatchViewModel

@Composable
fun WatchScreen(
    animeId: Long,
    initialEpisode: Int,
    onBackClick: () -> Unit,
    onSelectEpisode: (Int) -> Unit,
    viewModel: WatchViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    val player = remember { viewModel.getPlayer(context) }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.releasePlayer()
        }
    }

    LaunchedEffect(animeId, initialEpisode) {
        viewModel.loadAnime(animeId, initialEpisode)
    }

    LaunchedEffect(state.currentEpisode) {
        state.currentEpisode?.videoUrl?.let { url ->
            player.setMediaItem(MediaItem.fromUri(url))
            player.prepare()
            player.play()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            LoadingIndicator()
        } else if (state.anime != null && state.currentEpisode?.videoUrl == null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.VideocamOff,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Video Not Available",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "This episode doesn't have a video URL yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(Color.Black)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { viewModel.toggleControls() }
            ) {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = player
                            setUseController(false)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                AnimatedVisibility(
                    visible = state.showControls,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                    ) {
                        // Top bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .align(Alignment.TopCenter),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Text(
                                state.anime?.title ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { viewModel.toggleEpisodeSelector() }) {
                                Icon(Icons.Default.List, contentDescription = "Episodes", tint = Color.White)
                            }
                        }

                        // Center Play/Pause
                        IconButton(
                            onClick = { if (player.isPlaying) player.pause() else player.play() },
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Icon(
                                if (player.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        // Bottom controls
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                        ) {
                            Text(
                                "Episode ${state.currentEpisode?.episodeNumber ?: initialEpisode}",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            state.currentEpisode?.title?.let { title ->
                                Text(
                                    title,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = if (player.duration > 0) player.currentPosition.toFloat() / player.duration.toFloat() else 0f,
                                modifier = Modifier.fillMaxWidth(),
                                color = Primary,
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(formatTime(player.currentPosition), style = MaterialTheme.typography.bodySmall, color = Color.White)
                                Text(formatTime(player.duration), style = MaterialTheme.typography.bodySmall, color = Color.White)
                            }
                        }
                    }
                }
            }

            // Episode Selector
            AnimatedVisibility(
                visible = state.showEpisodeSelector,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.6f),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Episodes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { viewModel.toggleEpisodeSelector() }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.episodes) { episode ->
                                EpisodeItem(
                                    episodeNumber = episode.episodeNumber,
                                    title = episode.title,
                                    isWatched = false,
                                    isCurrent = episode.episodeNumber == (state.currentEpisode?.episodeNumber ?: initialEpisode),
                                    onClick = {
                                        onSelectEpisode(episode.episodeNumber)
                                        viewModel.selectEpisode(episode.episodeNumber)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}
