package com.anix.rx.domain.usecase

import com.anix.rx.domain.repository.AnimeRepository
import javax.inject.Inject

class GetAnimeByIdUseCase @Inject constructor(
    private val animeRepository: AnimeRepository
) {
    suspend operator fun invoke(id: Long) = 
        animeRepository.getAnimeById(id)
}
