package com.anix.rx.domain.usecase

import com.anix.rx.domain.repository.FavoritesRepository
import javax.inject.Inject

class GetFavoritesUseCase @Inject constructor(
    private val favoritesRepository: FavoritesRepository
) {
    suspend operator fun invoke() = favoritesRepository.getFavorites()
}
