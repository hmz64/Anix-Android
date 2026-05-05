package com.anix.rx.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anix.rx.data.model.User
import com.anix.rx.data.model.UserProfile
import com.anix.rx.data.model.WatchHistoryItem
import com.anix.rx.data.model.FavoriteItem
import com.anix.rx.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val isLoading: Boolean = true,
    val user: User? = null,
    val profile: UserProfile? = null,
    val watchHistory: List<WatchHistoryItem> = emptyList(),
    val favorites: List<FavoriteItem> = emptyList()
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val getWatchHistoryUseCase: GetWatchHistoryUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {
    
    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()
    
    init {
        loadProfile()
    }
    
    fun loadProfile() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            getProfileUseCase().onSuccess { profile ->
                val user = User(
                    id = profile.id,
                    username = profile.username,
                    email = profile.email,
                    role = profile.role,
                    avatar = profile.avatar,
                    bio = profile.bio,
                    createdAt = profile.createdAt ?: ""
                )
                _state.update { 
                    it.copy(
                        isLoading = false,
                        user = user,
                        profile = profile
                    )
                }
            }.onFailure {
                _state.update { it.copy(isLoading = false) }
            }
            
            getWatchHistoryUseCase().onSuccess { history ->
                _state.update { it.copy(watchHistory = history) }
            }
            
            getFavoritesUseCase().onSuccess { fav ->
                _state.update { it.copy(favorites = fav) }
            }
        }
    }
    
    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }
    
    fun updateProfile(bio: String?, avatar: String?) {
        viewModelScope.launch {
            updateProfileUseCase(bio, avatar)
            loadProfile()
        }
    }
}
