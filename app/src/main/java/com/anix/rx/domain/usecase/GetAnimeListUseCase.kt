package com.anix.rx.domain.usecase

import com.anix.rx.domain.repository.AnimeRepository
import javax.inject.Inject

class GetAnimeListUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {
    suspend operator fun invoke(query: String? = null) = 
        animeRepository.getAnimeList(query)
}
