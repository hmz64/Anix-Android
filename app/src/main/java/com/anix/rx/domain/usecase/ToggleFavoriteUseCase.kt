package com.anix.rx.domain.usecase

import com.anix.rx.domain.repository.FavoritesRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) {
    suspend operator fun invoke(animeId: Long, isFavorite: Boolean) {
        if (isFavorite) {
            favoritesRepository.deleteFavorite(animeId)
        } else {
            favoritesRepository.addFavorite(animeId)
        }
    }
}
