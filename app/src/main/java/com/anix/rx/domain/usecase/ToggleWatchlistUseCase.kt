package com.anix.rx.domain.usecase

import com.anix.rx.domain.repository.WatchlistRepository
import javax.inject.Inject

class ToggleWatchlistUseCase @Inject constructor(
    private val watchlistRepository: WatchlistRepository
) {
    suspend operator fun invoke(animeId: Long, inWatchlist: Boolean) {
        if (inWatchlist) {
            watchlistRepository.removeFromWatchlist(animeId)
        } else {
            watchlistRepository.addToWatchlist(animeId)
        }
    }
}
