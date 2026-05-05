package com.anix.rx.domain.usecase

import com.anix.rx.domain.repository.CommentRepository
import javax.inject.Inject

class GetCommentsUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(animeId: Long) = 
        commentRepository.getComments(animeId)
}
