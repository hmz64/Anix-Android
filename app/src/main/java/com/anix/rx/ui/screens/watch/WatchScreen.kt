package com.anix.rx.ui.screens.watch

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.anix.rx.data.model.Anime
import com.anix.rx.data.model.Episode
import com.anix.rx.domain.repository.AnimeRepository
import com.anix.rx.domain.repository.WatchRepository
import com.anix.rx.ui.components.*
import com.anix.rx.ui.theme.Primary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WatchState(
    val isLoading: Boolean = true,
    val anime: Anime? = null,
    val currentEpisode: Episode? = null,
    val episodes: List<Episode> = emptyList(),
    val isPlaying: Boolean = false,
    val showControls: Boolean = true,
    val showEpisodeSelector: Boolean = false
)

@HiltViewModel
class WatchViewModel @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val watchRepository: WatchRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(WatchState())
    val state: StateFlow<WatchState> = _state
    
    fun loadAnime(animeId: Long, episodeNumber: Int) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = animeRepository.getAnimeById(animeId)
            result.onSuccess { anime ->
                val ep = anime.episodes.find { it.episodeNumber == episodeNumber }
                _state.value = _state.value.copy(
                    isLoading = false,
                    anime = anime,
                    currentEpisode = ep,
                    episodes = anime.episodes
                )
            }
        }
    }
    
    fun updateProgress(progress: Long, duration: Long) {
        viewModelScope.launch {
            val animeId = _state.value.anime?.id ?: return@launch
            val epNum = _state.value.currentEpisode?.episodeNumber ?: return@launch
            watchRepository.updateHistory(
                animeId = animeId,
                episodeNumber = epNum,
                progress = progress,
                duration = duration,
                completed = progress >= duration - 10
            )
        }
    }
    
    fun toggleControls() {
        _state.value = _state.value.copy(showControls = !_state.value.showControls)
    }
    
    fun toggleEpisodeSelector() {
        _state.value = _state.value.copy(showEpisodeSelector = !_state.value.showEpisodeSelector)
    }
    
    fun selectEpisode(episodeNumber: Int) {
        val ep = _state.value.episodes.find { it.episodeNumber == episodeNumber }
        _state.value = _state.value.copy(
            currentEpisode = ep,
            showEpisodeSelector = false
        )
    }
}

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
    
    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    
    LaunchedEffect(animeId, initialEpisode) {
        viewModel.loadAnime(animeId, initialEpisode)
    }
    
    DisposableEffect(Unit) {
        onDispose {
            player?.release()
        }
    }
    
    // Initialize ExoPlayer
    LaunchedEffect(state.currentEpisode) {
        state.currentEpisode?.videoUrl?.let { url ->
            player?.let { p ->
                p.setMediaItem(MediaItem.fromUri(url))
                p.prepare()
                p.play()
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            LoadingIndicator()
        } else {
            // Video Player
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
                        ExoPlayer.Builder(ctx).build().also { exo ->
                            player = exo
                            state.currentEpisode?.videoUrl?.let { url ->
                                exo.setMediaItem(MediaItem.fromUri(url))
                                exo.prepare()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // Custom Controls Overlay
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
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Text(
                                state.anime?.title ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { viewModel.toggleEpisodeSelector() }) {
                                Icon(
                                    Icons.Default.List,
                                    contentDescription = "Episodes",
                                    tint = Color.White
                                )
                            }
                        }
                        
                        // Episode info
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                        ) {
                            Text(
                                "Episode ${state.currentEpisode?.episodeNumber ?: initialEpisode}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            state.currentEpisode?.title?.let { title ->
                                Text(
                                    title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }
            }
            
            // Episode Selector Bottom Sheet
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
                            Text(
                                "Episodes",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
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