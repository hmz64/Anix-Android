package com.anix.rx.ui.screens.browse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anix.rx.data.model.Anime
import com.anix.rx.domain.repository.AnimeRepository
import com.anix.rx.ui.components.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BrowseState(
    val isLoading: Boolean = false,
    val anime: List<Anime> = emptyList(),
    val filteredAnime: List<Anime> = emptyList(),
    val searchQuery: String = "",
    val selectedGenre: String? = null,
    val genres: List<String> = listOf("Action", "Adventure", "Comedy", "Drama", "Fantasy", "Horror", "Isekai", "Mecha", "Mystery", "Psychological", "Romance", "Sci-Fi", "Slice of Life", "Sports", "Supernatural", "Thriller")
)

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val animeRepository: AnimeRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(BrowseState())
    val state: StateFlow<BrowseState> = _state
    
    init {
        loadAnime()
    }
    
    private fun loadAnime() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val result = animeRepository.getAnimeList()
            result.onSuccess { list ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    anime = list,
                    filteredAnime = list
                )
            }
            result.onFailure {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
    
    fun search(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        filterAnime()
    }
    
    fun selectGenre(genre: String?) {
        _state.value = _state.value.copy(selectedGenre = genre)
        filterAnime()
    }
    
    private fun filterAnime() {
        val filtered = _state.value.anime.filter { anime ->
            val matchesSearch = _state.value.searchQuery.isEmpty() ||
                anime.title.contains(_state.value.searchQuery, ignoreCase = true)
            val matchesGenre = _state.value.selectedGenre == null ||
                anime.genres?.contains(_state.value.selectedGenre!!) == true
            matchesSearch && matchesGenre
        }
        _state.value = _state.value.copy(filteredAnime = filtered)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseScreen(
    onAnimeClick: (Long) -> Unit,
    viewModel: BrowseViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Browse", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.search(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search anime...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.search("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                shape = MaterialTheme.shapes.large
            )
            
            // Genre Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = state.selectedGenre == null,
                        onClick = { viewModel.selectGenre(null) },
                        label = { Text("All") }
                    )
                }
                items(state.genres) { genre ->
                    FilterChip(
                        selected = state.selectedGenre == genre,
                        onClick = { viewModel.selectGenre(genre) },
                        label = { Text(genre) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Results count
            Text(
                "${state.filteredAnime.size} results",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            // Anime Grid
            if (state.isLoading) {
                LoadingIndicator()
            } else if (state.filteredAnime.isEmpty()) {
                EmptyState(
                    title = "No anime found",
                    message = "Try adjusting your search or filters"
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredAnime) { anime ->
                        AnimeCard(
                            anime = anime,
                            onClick = { onAnimeClick(anime.id) }
                        )
                    }
                }
            }
        }
    }
}