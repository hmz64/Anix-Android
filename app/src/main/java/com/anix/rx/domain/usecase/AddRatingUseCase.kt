package com.anix.rx.domain.usecase

import com.anix.rx.domain.repository.AnimeRepository
import javax.inject.Inject

class AddRatingUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {
    suspend operator fun invoke(animeId: Long, score: Int) = 
        animeRepository.addRating(animeId, score)
}
