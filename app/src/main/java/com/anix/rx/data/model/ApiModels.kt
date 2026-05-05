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
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class AuthResponse(
    val success: Boolean = false,
    val message: String? = null,
    val token: String? = null,
    @SerialName("refresh_token")
    val refreshToken: String? = null,
    val user: User? = null
)

@Serializable
data class Anime(
    val id: Long = 0,
    val title: String = "",
    val slug: String = "",
    @SerialName("alternative_title")
    val alternativeTitle: String? = null,
    val synopsis: String? = null,
    val thumbnail: String? = null,
    val banner: String? = null,
    // Fix: genres dari backend adalah List<String>, bukan String?
    val genres: List<String> = emptyList(),
    val year: Int? = null,
    val status: String = "ongoing",
    val rating: Double = 0.0,
    @SerialName("rating_count")
    val ratingCount: Int = 0,
    @SerialName("total_episodes")
    val totalEpisodes: Int = 0,
    val views: Long = 0,
    val favorites: Int = 0,
    @SerialName("mal_id")
    val malId: Int? = null,
    val episodes: List<Episode> = emptyList()
)

@Serializable
data class Episode(
    val id: Long = 0,
    @SerialName("anime_id")
    val animeId: Long = 0,
    @SerialName("episode_number")
    val episodeNumber: Int = 0,
    val title: String? = null,
    @SerialName("video_url")
    val videoUrl: String? = null,
    val thumbnail: String? = null,
    @SerialName("subtitle_url")
    val subtitleUrl: String? = null,
    @SerialName("duration_sec")
    val durationSec: Long = 0
)

@Serializable
data class Comment(
    val id: Long = 0,
    @SerialName("user_id")
    val userId: Long = 0,
    val username: String = "",
    val avatar: String? = null,
    @SerialName("anime_id")
    val animeId: Long = 0,
    val content: String = "",
    @SerialName("created_at")
    val createdAt: String? = null,
    val likes: Int = 0,
    @SerialName("parent_id")
    val parentId: Long? = null,
    val replies: List<Comment> = emptyList()
)

// Fix: WatchHistoryItem sesuai dengan WatchHistoryEntity - tambah id dan userId
@Serializable
data class WatchHistoryItem(
    val id: Long = 0,
    @SerialName("user_id")
    val userId: Long = 0,
    @SerialName("anime_id")
    val animeId: Long = 0,
    val title: String = "",
    val slug: String = "",
    val thumbnail: String? = null,
    @SerialName("episode_number")
    val episodeNumber: Int = 0,
    val progress: Long = 0,
    val duration: Long = 0,
    val completed: Boolean = false,
    @SerialName("last_watched")
    val lastWatched: String? = null,
    val percent: Int = 0
)

@Serializable
data class FavoriteItem(
    val id: Long = 0,
    val title: String = "",
    val slug: String = "",
    val thumbnail: String? = null,
    val year: Int? = null,
    val status: String = "",
    val rating: Double = 0.0,
    @SerialName("total_episodes")
    val totalEpisodes: Int = 0,
    @SerialName("favorited_at")
    val favoritedAt: String? = null
)

@Serializable
data class Rating(
    val id: Long = 0,
    @SerialName("user_id")
    val userId: Long = 0,
    @SerialName("anime_id")
    val animeId: Long = 0,
    val score: Int = 0,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class Notification(
    val id: Long = 0,
    val type: String = "",
    val title: String = "",
    val message: String? = null,
    val link: String? = null,
    @SerialName("is_read")
    val isRead: Boolean = false,
    @SerialName("created_at")
    val createdAt: String? = null
)

@Serializable
data class UserProfile(
    val id: Long = 0,
    val username: String = "",
    val email: String = "",
    val role: String = "user",
    val avatar: String? = null,
    val bio: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    val stats: UserStats? = null
)

@Serializable
data class UserStats(
    @SerialName("watchlist_count")
    val watchlistCount: Long = 0,
    @SerialName("favorites_count")
    val favoritesCount: Long = 0,
    @SerialName("comments_count")
    val commentsCount: Long = 0,
    @SerialName("episodes_watched")
    val episodesWatched: Long = 0
)

@Serializable
data class AdminStats(
    @SerialName("total_anime")
    val totalAnime: Long = 0,
    @SerialName("total_episodes")
    val totalEpisodes: Long = 0,
    @SerialName("total_users")
    val totalUsers: Long = 0,
    @SerialName("total_comments")
    val totalComments: Long = 0,
    @SerialName("total_views")
    val totalViews: Long = 0
)
