package com.anix.rx.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anix.rx.domain.usecase.AddRatingUseCase
import com.anix.rx.domain.usecase.GetAnimeByIdUseCase
import com.anix.rx.domain.usecase.GetCommentsUseCase
import com.anix.rx.domain.usecase.AddCommentUseCase
import com.anix.rx.domain.usecase.ToggleFavoriteUseCase
import com.anix.rx.domain.usecase.ToggleWatchlistUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailState(
    val isLoading: Boolean = true,
    val anime: com.anix.rx.data.model.Anime? = null,
    val isFavorite: Boolean = false,
    val inWatchlist: Boolean = false,
    val userRating: Int = 0,
    val comments: List<com.anix.rx.data.model.Comment> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getAnimeByIdUseCase: GetAnimeByIdUseCase,
    private val addRatingUseCase: AddRatingUseCase,
    private val getCommentsUseCase: GetCommentsUseCase,
    private val addCommentUseCase: AddCommentUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val toggleWatchlistUseCase: ToggleWatchlistUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(DetailState())
    val state: StateFlow<DetailState> = _state
    
    fun loadAnime(id: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            getAnimeByIdUseCase(id).onSuccess { anime ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    anime = anime
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load anime"
                )
            }
            // Load comments
            getCommentsUseCase(id).onSuccess { comments ->
                _state.value = _state.value.copy(comments = comments)
            }
        }
    }
    
    fun setRating(animeId: Long, score: Int) {
        viewModelScope.launch {
            addRatingUseCase(animeId, score)
            _state.value = _state.value.copy(userRating = score)
        }
    }
    
    fun addComment(animeId: Long, content: String, parentId: Long? = null) {
        viewModelScope.launch {
            addCommentUseCase(animeId, content, parentId)
            // Reload comments
            loadAnime(animeId)
        }
    }
    
    fun toggleFavorite() {
        viewModelScope.launch {
            val animeId = _state.value.anime?.id ?: return@launch
            toggleFavoriteUseCase(animeId)
            _state.value = _state.value.copy(isFavorite = !_state.value.isFavorite)
        }
    }
    
    fun toggleWatchlist() {
        viewModelScope.launch {
            val animeId = _state.value.anime?.id ?: return@launch
            toggleWatchlistUseCase(animeId)
            _state.value = _state.value.copy(inWatchlist = !_state.value.inWatchlist)
        }
    }
}
