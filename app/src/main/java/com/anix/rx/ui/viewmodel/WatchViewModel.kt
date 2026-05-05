package com.anix.rx.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.Player
import com.anix.rx.data.model.Anime
import com.anix.rx.data.model.Episode
import com.anix.rx.domain.repository.AnimeRepository
import com.anix.rx.domain.repository.WatchRepository
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
    private var player: ExoPlayer? = null
    private var currentProgress = 0L
    private var currentDuration = 0L

    // Listener untuk auto-save progress
    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)
            if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
                saveProgress()
            }
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            super.onPositionDiscontinuity(oldPosition, newPosition, reason)
            currentProgress = newPosition.positionMs
            currentDuration = player?.duration ?: 0L
        }
    }

    fun getPlayer(context: Context): ExoPlayer {
        if (player == null) {
            player = ExoPlayer.Builder(context).build().apply {
                playWhenReady = true
                addListener(playerListener)
            }
        }
        return player!!
    }

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
            }.onFailure {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun updateProgress(progress: Long, duration: Long) {
        viewModelScope.launch {
            val animeId = _state.value.anime?.id ?: return@launch
            val epNum = _state.value.currentEpisode?.episodeNumber ?: return@launch
            watchRepository.updateWatchHistory(
                animeId = animeId,
                episodeNumber = epNum,
                progress = progress,
                duration = duration,
                completed = progress >= duration - 10_000
            )
        }
    }

    private fun saveProgress() {
        val progress = player?.currentPosition ?: return
        val duration = player?.duration ?: return
        if (duration > 0) {
            updateProgress(progress, duration)
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
        ep?.videoUrl?.let { url ->
            player?.setMediaItem(MediaItem.fromUri(url))
            player?.prepare()
            player?.play()
        }
    }

    override fun onCleared() {
        super.onCleared()
        player?.removeListener(playerListener)
        player?.release()
        player = null
    }
}
