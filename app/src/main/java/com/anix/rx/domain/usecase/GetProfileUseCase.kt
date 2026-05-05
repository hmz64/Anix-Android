package com.anix.rx.domain.usecase

import com.anix.rx.domain.repository.ProfileRepository
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke() = profileRepository.getProfile()
}
