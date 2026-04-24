package com.anix.rx.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.anix.rx.data.model.*
import com.anix.rx.domain.repository.*
import com.anix.rx.ui.components.*
import com.anix.rx.ui.theme.Primary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailState(
    val isLoading: Boolean = true,
    val anime: Anime? = null,
    val isFavorite: Boolean = false,
    val inWatchlist: Boolean = false,
    val userRating: Int = 0,
    val error: String? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val favoritesRepository: FavoritesRepository,
    private val watchlistRepository: WatchlistRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state
    
    fun loadAnime(id: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = animeRepository.getAnimeById(id)
            result.onSuccess { anime ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    anime = anime
                )
            }
            result.onFailure {
                _state.value = _state.value.copy(isLoading = false, error = "Failed to load anime")
            }
        }
    }
    
    fun toggleFavorite() {
        viewModelScope.launch {
            val animeId = _state.value.anime?.id ?: return@launch
            if (_state.value.isFavorite) {
                favoritesRepository.deleteFavorite(animeId)
                _state.value = _state.value.copy(isFavorite = false)
            } else {
                favoritesRepository.addFavorite(animeId)
                _state.value = _state.value.copy(isFavorite = true)
            }
        }
    }
    
    fun toggleWatchlist() {
        viewModelScope.launch {
            val animeId = _state.value.anime?.id ?: return@launch
            if (_state.value.inWatchlist) {
                watchlistRepository.removeFromWatchlist(animeId)
                _state.value = _state.value.copy(inWatchlist = false)
            } else {
                watchlistRepository.addToWatchlist(animeId)
                _state.value = _state.value.copy(inWatchlist = true)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeDetailScreen(
    animeId: Long,
    onBackClick: () -> Unit,
    onWatchEpisode: (Int) -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    LaunchedEffect(animeId) {
        viewModel.loadAnime(animeId)
    }
    
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            if (state.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (state.isFavorite) Primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = { viewModel.toggleWatchlist() }) {
                        Icon(
                            if (state.inWatchlist) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Watchlist"
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingIndicator()
        } else if (state.anime != null) {
            val anime = state.anime!!
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                    ) {
                        AsyncImage(
                            model = anime.banner ?: anime.thumbnail,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            androidx.compose.ui.graphics.Color.Transparent,
                                            MaterialTheme.colorScheme.background
                                        )
                                    )
                                )
                        )
                    }
                }
                
                // Info
                item {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            anime.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            RatingBadge(rating = anime.rating)
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    anime.status,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Text(
                                "${anime.totalEpisodes} episodes",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        anime.genres?.let { genres ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                genres,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        anime.synopsis?.let { syn ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                syn,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // Episodes
                item {
                    Text(
                        "Episodes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                items(anime.episodes) { episode ->
                    EpisodeItem(
                        episodeNumber = episode.episodeNumber,
                        title = episode.title,
                        isWatched = false,
                        isCurrent = false,
                        onClick = { onWatchEpisode(episode.episodeNumber) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}