package com.anix.rx.domain.usecase

import com.anix.rx.domain.repository.CommentRepository
import javax.inject.Inject

class AddCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository
) {
    suspend operator fun invoke(animeId: Long, content: String, parentId: Long? = null) = 
        commentRepository.addComment(animeId, content, parentId)
}
