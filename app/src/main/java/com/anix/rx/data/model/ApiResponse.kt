package com.anix.rx.data.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val success: Boolean = true,
    val message: String? = null,
    val data: T? = null
)

@Serializable
data class UserProfile(
    val user: User? = null,
    val stats: UserStats? = null
)

@Serializable
data class UserStats(
    val episodesWatched: Long = 0,
    val animeCompleted: Long = 0,
    val favoritesCount: Long = 0,
    val commentsCount: Long = 0,
    val watchTimeMinutes: Long = 0
)