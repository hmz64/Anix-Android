package com.anix.rx.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: Long = 0,
    val username: String = "",
    val email: String = "",
    val role: String = "user",
    val avatar: String? = null,
    val bio: String? = null,
    val createdAt: String? = null
)

@Serializable
data class AuthResponse(
    val success: Boolean = false,
    val message: String? = null,
    val token: String? = null,
    val refreshToken: String? = null,
    val user: User? = null
)

@Serializable
data class Anime(
    val id: Long = 0,
    val title: String = "",
    val slug: String = "",
    val synopsis: String? = null,
    val thumbnail: String? = null,
    val banner: String? = null,
    val genres: String? = null,
    val year: Int? = null,
    val status: String = "ongoing",
    val rating: Double = 0.0,
    val totalEpisodes: Int = 0,
    val views: Long = 0,
    val episodes: List<Episode> = emptyList()
)

@Serializable
data class Episode(
    val id: Long = 0,
    val animeId: Long = 0,
    val episodeNumber: Int = 0,
    val title: String? = null,
    val videoUrl: String? = null,
    val thumbnail: String? = null,
    val subtitleUrl: String? = null,
    val durationSec: Long = 0,
    val introStart: Int = 0,
    val introEnd: Int = 0,
    val outroStart: Int = 0
)

@Serializable
data class Comment(
    val id: Long = 0,
    val userId: Long = 0,
    val username: String = "",
    val avatar: String? = null,
    val animeId: Long = 0,
    val episodeNumber: Int? = null,
    val content: String = "",
    val createdAt: String? = null,
    val likes: Int = 0,
    val userLiked: Boolean = false,
    val replies: List<Comment> = emptyList()
)

@Serializable
data class WatchHistoryItem(
    val animeId: Long = 0,
    val animeTitle: String = "",
    val animeThumbnail: String? = null,
    val episodeNumber: Int = 0,
    val progress: Long = 0,
    val duration: Long = 0,
    val completed: Boolean = false,
    val lastWatched: String? = null
)

@Serializable
data class FavoriteItem(
    val animeId: Long = 0,
    val animeTitle: String = "",
    val thumbnail: String? = null,
    val rating: Double = 0.0,
    val addedAt: String? = null
)

@Serializable
data class Rating(
    val id: Long = 0,
    val userId: Long = 0,
    val animeId: Long = 0,
    val score: Int = 0,
    val createdAt: String? = null
)

@Serializable
data class Notification(
    val id: Long = 0,
    val userId: Long = 0,
    val type: String = "",
    val title: String = "",
    val message: String = "",
    val link: String? = null,
    val read: Boolean = false,
    val createdAt: String? = null
)

@Serializable
data class AdminStats(
    val totalAnime: Long = 0,
    val totalEpisodes: Long = 0,
    val totalUsers: Long = 0,
    val totalComments: Long = 0,
    val totalViews: Long = 0,
    val newUsersToday: Long = 0
) {
    @SerialName("total_anime")
    val totalAnime2: Long = 0
    
    @SerialName("total_episodes")
    val totalEpisodes2: Long = 0
}