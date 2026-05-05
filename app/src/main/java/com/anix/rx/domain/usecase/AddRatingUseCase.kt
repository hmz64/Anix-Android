package com.anix.rx.domain.usecase

import com.anix.rx.data.api.AniXApi
import javax.inject.Inject

class AddRatingUseCase @Inject constructor(private val api: AniXApi) {
    suspend operator fun invoke(animeId: Long, score: Int) = runCatching {
        api.addRating(mapOf("anime_id" to animeId, "score" to score))
    }
}
