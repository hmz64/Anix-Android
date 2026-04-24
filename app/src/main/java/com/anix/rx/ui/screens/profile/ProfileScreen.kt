package com.anix.rx.ui.screens.profile

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

data class ProfileState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val profile: UserProfile? = null,
    val watchHistory: List<WatchHistoryItem> = emptyList(),
    val favorites: List<FavoriteItem> = emptyList()
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val profileRepository: ProfileRepository,
    private val watchRepository: WatchRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state
    
    init {
        loadProfile()
    }
    
    fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            profileRepository.getProfile().onSuccess { profile ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    user = profile.user,
                    profile = profile
                )
            }
            watchRepository.getWatchHistory().onSuccess { history ->
                _state.value = _state.value.copy(watchHistory = history)
            }
            favoritesRepository.getFavorites().onSuccess { fav ->
                _state.value = _state.value.copy(favorites = fav)
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Profile", "History", "Favorites")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.logout()
                            onLogout()
                        }
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingIndicator()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Profile Header
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar
                        AsyncImage(
                            model = state.user?.avatar,
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(100.dp)
                                .clip(MaterialTheme.shapes.extraLarge),
                            contentScale = ContentScale.Crop
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            state.user?.username ?: "Username",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            state.user?.email ?: "email@example.com",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Surface(
                            color = Primary,
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                state.user?.role?.replaceFirstChar { it.uppercase() } ?: "User",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }
                }
                
                // Stats
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatCard("Watched", "${state.profile?.stats?.episodesWatched ?: 0}")
                        StatCard("Completed", "${state.profile?.stats?.animeCompleted ?: 0}")
                        StatCard("Favorites", "${state.favorites.size}")
                    }
                }
                
                // Tabs
                item {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) }
                            )
                        }
                    }
                }
                
                // Tab Content
                when (selectedTab) {
                    0 -> {
                        // Bio Edit
                        item {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "Bio",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    state.user?.bio ?: "No bio yet",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                    1 -> {
                        // Watch History
                        items(state.watchHistory) { item ->
                            ListItem(
                                headlineContent = { Text(item.animeTitle) },
                                supportingContent = { Text("Episode ${item.episodeNumber}") },
                                leadingContent = {
                                    AsyncImage(
                                        model = item.animeThumbnail,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(MaterialTheme.shapes.small)
                                    )
                                }
                            )
                        }
                    }
                    2 -> {
                        // Favorites
                        items(state.favorites) { item ->
                            ListItem(
                                headlineContent = { Text(item.animeTitle) },
                                supportingContent = { Text("Rating: ${item.rating}") },
                                leadingContent = {
                                    AsyncImage(
                                        model = item.thumbnail,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(MaterialTheme.shapes.small)
                                    )
                                }
                            )
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String) {
    Card(
        modifier = Modifier.width(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}