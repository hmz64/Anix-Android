package com.anix.rx.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anix.rx.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state
    
    fun register(username: String, email: String, password: String, confirmPassword: String) {
        when {
            username.isBlank() || email.isBlank() || password.isBlank() -> {
                _state.value = _state.value.copy(error = "Please fill all fields")
                return
            }
            password.length < 8 -> {
                _state.value = _state.value.copy(error = "Password must be at least 8 characters")
                return
            }
            password != confirmPassword -> {
                _state.value = _state.value.copy(error = "Passwords do not match")
                return
            }
        }
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val result = registerUseCase(username, email, password)
            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        _state.value = _state.value.copy(isLoading = false, isSuccess = true)
                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = response.message ?: "Registration failed"
                        )
                    }
                },
                onFailure = { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message ?: "An error occurred"
                    )
                }
            )
        }
    }
    
    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
