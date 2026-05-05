package com.anix.rx.domain.usecase

import com.anix.rx.domain.repository.AnimeRepository
import javax.inject.Inject

class GetTrendingUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {
    suspend operator fun invoke() = 
        animeRepository.getTrending()
}
