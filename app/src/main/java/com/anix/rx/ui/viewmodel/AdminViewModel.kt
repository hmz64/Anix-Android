package com.anix.rx.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anix.rx.data.model.AdminStats
import com.anix.rx.domain.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminUiState(val isLoading: Boolean = true, val stats: AdminStats? = null, val error: String? = null)

@HiltViewModel
class AdminViewModel @Inject constructor(private val adminRepository: AdminRepository) : ViewModel() {
    private val _state = MutableStateFlow(AdminUiState())
    val state: StateFlow<AdminUiState> = _state
    init { loadStats() }
    fun loadStats() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            adminRepository.getStats()
                .onSuccess { _state.value = AdminUiState(isLoading = false, stats = it) }
                .onFailure { _state.value = AdminUiState(isLoading = false, error = it.message) }
        }
    }
}
