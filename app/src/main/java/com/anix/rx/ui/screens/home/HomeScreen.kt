package com.anix.rx.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anix.rx.data.model.Anime
import com.anix.rx.domain.repository.AnimeRepository
import com.anix.rx.domain.repository.AuthRepository
import com.anix.rx.ui.components.*
import com.anix.rx.ui.theme.Primary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val isLoading: Boolean = true,
    val trending: List<Anime> = emptyList(),
    val recentlyAdded: List<Anime> = emptyList(),
    val ongoing: List<Anime> = emptyList(),
    val error: String? = null,
    val userRole: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val animeRepository: AnimeRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            
            // Load all sections
            val trendingResult = animeRepository.getTrending()
            val recentResult = animeRepository.getRecentlyAdded()
            val ongoingResult = animeRepository.getAnimeList()
            
            _state.value = _state.value.copy(
                isLoading = false,
                trending = trendingResult.getOrElse { emptyList() },
                recentlyAdded = recentResult.getOrElse { emptyList() },
                ongoing = ongoingResult.getOrElse { emptyList() }.filter { it.status == "ongoing" }
            )
            
            // Get user role
            authRepository.getUserRole().collect { role ->
                _state.value = _state.value.copy(userRole = role)
            }
        }
    }
    
    fun refresh() {
        loadData()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAnimeClick: (Long) -> Unit,
    onProfileClick: () -> Unit,
    onModeratorClick: () -> Unit,
    onAdminClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "ANIX",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }
                    if (state.userRole == "moderator" || state.userRole == "admin") {
                        IconButton(onClick = onModeratorClick) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = "Moderator")
                        }
                    }
                    if (state.userRole == "admin") {
                        IconButton(onClick = onAdminClick) {
                            Icon(Icons.Default.Settings, contentDescription = "Admin")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingIndicator()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Trending Section
                item {
                    SectionHeader(title = "Trending 🎬")
                    
                    if (state.trending.isEmpty()) {
                        EmptyState(
                            title = "No trending anime",
                            message = "Check back later!",
                            modifier = Modifier.height(200.dp)
                        )
                    } else {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.trending) { anime ->
                                AnimeCard(
                                    anime = anime,
                                    onClick = { onAnimeClick(anime.id) },
                                    modifier = Modifier.width(160.dp)
                                )
                            }
                        }
                    }
                }
                
                // Ongoing Section
                item {
                    if (state.ongoing.isNotEmpty()) {
                        SectionHeader(title = "Ongoing 📺")
                        
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.ongoing) { anime ->
                                AnimeCard(
                                    anime = anime,
                                    onClick = { onAnimeClick(anime.id) },
                                    modifier = Modifier.width(160.dp)
                                )
                            }
                        }
                    }
                }
                
                // Recently Added Section
                item {
                    if (state.recentlyAdded.isNotEmpty()) {
                        SectionHeader(title = "Recently Added 🆕")
                        
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.recentlyAdded) { anime ->
                                AnimeCard(
                                    anime = anime,
                                    onClick = { onAnimeClick(anime.id) },
                                    modifier = Modifier.width(160.dp)
                                )
                            }
                        }
                    }
                }
                
                // All Anime Grid
                item {
                    SectionHeader(title = "All Anime")
                }
                
                items(state.trending + state.ongoing + state.recentlyAdded) { anime ->
                    AnimeCard(
                        anime = anime,
                        onClick = { onAnimeClick(anime.id) },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    )
                }
                
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}