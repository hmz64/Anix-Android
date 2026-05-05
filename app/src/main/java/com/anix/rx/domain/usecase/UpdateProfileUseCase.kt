package com.anix.rx.domain.usecase

import com.anix.rx.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(bio: String? = null, avatar: String? = null) = 
        profileRepository.updateProfile(bio, avatar)
}
