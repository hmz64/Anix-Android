package com.anix.rx.domain.usecase

import com.anix.rx.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, email: String, password: String) = 
        authRepository.register(username, email, password)
}
