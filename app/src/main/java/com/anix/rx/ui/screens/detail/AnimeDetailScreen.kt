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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.anix.rx.ui.components.*
import com.anix.rx.ui.theme.Primary
import com.anix.rx.ui.viewmodel.DetailViewModel

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
                                            Color.Transparent,
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
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
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
                
                // Rating Section
                item {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Your Rating:",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row {
                            repeat(5) { index ->
                                val starIndex = index + 1
                                IconButton(onClick = { viewModel.setRating(anime.id, starIndex) }) {
                                    Icon(
                                        if (state.userRating >= starIndex) Icons.Default.Star 
                                        else Icons.Default.StarBorder,
                                        contentDescription = "Star $starIndex",
                                        tint = if (state.userRating >= starIndex) Color(0xFFFFD700) 
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        Text(
                            "(${anime.ratingCount} ratings)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Episodes Header
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
                
                // Comments Header
                item {
                    Text(
                        "Comments",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                
                items(state.comments) { comment ->
                    CommentItem(
                        comment = comment,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
