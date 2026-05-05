package com.anix.rx.domain.usecase

import com.anix.rx.domain.repository.WatchRepository
import javax.inject.Inject

class UpdateWatchHistoryUseCase @Inject constructor(
    private val watchRepository: WatchRepository
) {
    suspend operator fun invoke(animeId: Long, episodeNumber: Int, progress: Long, duration: Long, completed: Boolean) = 
        watchRepository.updateHistory(animeId, episodeNumber, progress, duration, completed)
}
