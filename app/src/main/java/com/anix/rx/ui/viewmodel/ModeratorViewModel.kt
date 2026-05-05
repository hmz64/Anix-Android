package com.anix.rx.ui.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class ModeratorUiState(val isLoading: Boolean = false, val message: String = "Moderator panel coming soon")

@HiltViewModel
class ModeratorViewModel @Inject constructor() : ViewModel() {
    private val _state = MutableStateFlow(ModeratorUiState())
    val state: StateFlow<ModeratorUiState> = _state
}
