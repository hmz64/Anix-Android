package com.anix.rx.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anix.rx.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state
    
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _state.value = _state.value.copy(error = "Please fill all fields")
            return
        }
        
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            
            val result = loginUseCase(email, password)
            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        _state.value = _state.value.copy(isLoading = false, isSuccess = true)
                    } else {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = response.message ?: "Login failed"
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
