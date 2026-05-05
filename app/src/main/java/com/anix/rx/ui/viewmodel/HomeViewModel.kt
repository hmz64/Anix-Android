package com.anix.rx.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anix.rx.domain.repository.AnimeRepository
import com.anix.rx.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val isLoading: Boolean = true,
    val trending: List<com.anix.rx.data.model.Anime> = emptyList(),
    val recentlyAdded: List<com.anix.rx.data.model.Anime> = emptyList(),
    val ongoing: List<com.anix.rx.data.model.Anime> = emptyList(),
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
        // Collect user role with proper lifecycle handling
        authRepository.getUserRole()
            .onEach { role ->
                _state.value = _state.value.copy(userRole = role)
            }
            .launchIn(viewModelScope)
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val trendingResult = animeRepository.getTrending()
                val recentResult = animeRepository.getRecentlyAdded()
                val ongoingResult = animeRepository.getAnimeList()

                _state.value = _state.value.copy(
                    isLoading = false,
                    trending = trendingResult.getOrElse { emptyList() },
                    recentlyAdded = recentResult.getOrElse { emptyList() },
                    ongoing = ongoingResult.getOrElse { emptyList() }
                        .filter { it.status == "ongoing" }
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to load data: ${e.message}"
                )
            }
        }
    }

    fun refresh() {
        loadData()
    }
}
