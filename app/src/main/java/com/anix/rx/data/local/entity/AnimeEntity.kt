package com.anix.rx.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "anime")
data class AnimeEntity(
    @PrimaryKey
    val id: Long,
    val title: String,
    val slug: String,
    val alternativeTitle: String?,
    val synopsis: String?,
    val thumbnail: String?,
    val banner: String?,
    val year: Int?,
    val status: String,
    val rating: Double,
    val ratingCount: Int,
    val totalEpisodes: Int,
    val views: Long,
    val favorites: Int,
    val malId: Int?,
    val genres: List<String>, // Menggunakan List agar ditangani otomatis oleh Converters
    val createdAt: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "episodes")
data class EpisodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val animeId: Long,
    val episodeNumber: Int,
    val title: String?,
    val videoUrl: String?,
    val thumbnail: String?,
    val subtitleUrl: String?,
    val durationSec: Long,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Long,
    val username: String,
    val email: String,
    val role: String,
    val createdAt: String?,
    val avatar: String?,
    val bio: String?,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val animeId: Long,
    val episodeNumber: Int,
    val progress: Long,
    val duration: Long,
    val completed: Boolean,
    val lastWatched: String?,
    val lastUpdated: Long = System.currentTimeMillis()
)
