package com.anix.rx.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anix.rx.data.model.Anime
import com.anix.rx.domain.repository.AnimeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BrowseState(
    val isLoading: Boolean = false,
    val anime: List<Anime> = emptyList(),
    val filteredAnime: List<Anime> = emptyList(),
    val searchQuery: String = "",
    val selectedGenre: String? = null,
    val genres: List<String> = listOf(
        "Action", "Adventure", "Comedy", "Drama", "Fantasy", 
        "Horror", "Isekai", "Mecha", "Mystery", "Psychological", 
        "Romance", "Sci-Fi", "Slice of Life", "Sports", 
        "Supernatural", "Thriller"
    )
)

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val animeRepository: AnimeRepository
) : ViewModel() {
    
    private val _state = MutableStateFlow(BrowseState())
    val state: StateFlow<BrowseState> = _state.asStateFlow()
    
    init {
        loadAnime()
    }
    
    fun loadAnime() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val result = animeRepository.getAnimeList()
            result.onSuccess { list ->
                _state.update { 
                    it.copy(
                        isLoading = false,
                        anime = list,
                        filteredAnime = list
                    )
                }
            }
            result.onFailure {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
    
    fun search(query: String) {
        _state.update { it.copy(searchQuery = query, isLoading = true) }
        viewModelScope.launch {
            val result = animeRepository.getAnimeList(query)
            result.onSuccess { list ->
                _state.update { 
                    it.copy(
                        anime = list,
                        isLoading = false
                    )
                }
                filterAnime()
            }
            result.onFailure {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
    
    fun selectGenre(genre: String?) {
        _state.update { it.copy(selectedGenre = genre) }
        filterAnime()
    }
    
    private fun filterAnime() {
        val currentState = _state.value
        val filtered = currentState.anime.filter { anime ->
            val matchesSearch = currentState.searchQuery.isEmpty() ||
                anime.title.contains(currentState.searchQuery, ignoreCase = true)
            val matchesGenre = currentState.selectedGenre == null ||
                anime.genres?.contains(currentState.selectedGenre!!) == true
            matchesSearch && matchesGenre
        }
        _state.update { it.copy(filteredAnime = filtered) }
    }
}
