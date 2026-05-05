package com.anix.rx.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anix.rx.data.model.Anime
import com.anix.rx.data.model.Comment
import com.anix.rx.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DetailState(
    val isLoading: Boolean = true, val anime: Anime? = null,
    val isFavorite: Boolean = false, val inWatchlist: Boolean = false,
    val userRating: Int = 0, val comments: List<Comment> = emptyList(), val error: String? = null
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
            getAnimeByIdUseCase(id).onSuccess { _state.value = _state.value.copy(isLoading = false, anime = it) }
                .onFailure { _state.value = _state.value.copy(isLoading = false, error = "Failed to load anime") }
            getCommentsUseCase(id).onSuccess { _state.value = _state.value.copy(comments = it) }
        }
    }
    fun setRating(animeId: Long, score: Int) { viewModelScope.launch { addRatingUseCase(animeId, score); _state.value = _state.value.copy(userRating = score) } }
    fun addComment(animeId: Long, content: String, parentId: Long? = null) { viewModelScope.launch { addCommentUseCase(animeId, content, parentId); loadAnime(animeId) } }
    fun toggleFavorite() { viewModelScope.launch { val id = _state.value.anime?.id ?: return@launch; toggleFavoriteUseCase(id); _state.value = _state.value.copy(isFavorite = !_state.value.isFavorite) } }
    fun toggleWatchlist() { viewModelScope.launch { val id = _state.value.anime?.id ?: return@launch; toggleWatchlistUseCase(id); _state.value = _state.value.copy(inWatchlist = !_state.value.inWatchlist) } }
}
