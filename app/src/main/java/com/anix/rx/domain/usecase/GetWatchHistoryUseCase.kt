package com.anix.rx.domain.usecase

import com.anix.rx.domain.repository.WatchRepository
import javax.inject.Inject

class GetWatchHistoryUseCase @Inject constructor(
    private val watchRepository: WatchRepository
) {
    suspend operator fun invoke() = watchRepository.getWatchHistory()
}
