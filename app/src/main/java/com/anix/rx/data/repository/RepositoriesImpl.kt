package com.anix.rx.data.repository

import android.content.Context
import com.anix.rx.data.api.AniXApi
import com.anix.rx.data.api.PreferencesKeys
import com.anix.rx.data.api.getEncryptedPrefs
import com.anix.rx.data.local.dao.*
import com.anix.rx.data.local.entity.*
import com.anix.rx.data.model.*
import com.anix.rx.domain.repository.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AniXApi,
    @ApplicationContext private val context: Context
) : AuthRepository {
    private val encryptedPrefs = getEncryptedPrefs(context)

    override suspend fun login(email: String, password: String): Result<AuthResponse> = runCatching {
        val response = api.login(mapOf("username" to email, "password" to password))
        if (response.isSuccessful && response.body()?.success == true) {
            val auth = response.body()!!
            auth.token?.let { encryptedPrefs.edit().putString(PreferencesKeys.TOKEN, it).apply() }
            auth.refreshToken?.let { encryptedPrefs.edit().putString(PreferencesKeys.REFRESH_TOKEN, it).apply() }
            auth.user?.let { user ->
                encryptedPrefs.edit().putLong(PreferencesKeys.USER_ID, user.id).apply()
                encryptedPrefs.edit().putString(PreferencesKeys.USERNAME, user.username).apply()
                encryptedPrefs.edit().putString(PreferencesKeys.ROLE, user.role).apply()
            }
            auth
        } else throw Exception(response.body()?.message ?: "Login failed")
    }

    override suspend fun register(username: String, email: String, password: String): Result<AuthResponse> = runCatching {
        val response = api.register(mapOf("username" to username, "email" to email, "password" to password))
        if (response.isSuccessful && response.body()?.success == true) {
            val auth = response.body()!!
            auth.token?.let { encryptedPrefs.edit().putString(PreferencesKeys.TOKEN, it).apply() }
            auth.user?.let { user ->
                encryptedPrefs.edit().putLong(PreferencesKeys.USER_ID, user.id).apply()
                encryptedPrefs.edit().putString(PreferencesKeys.USERNAME, user.username).apply()
                encryptedPrefs.edit().putString(PreferencesKeys.ROLE, user.role).apply()
            }
            auth
        } else throw Exception(response.body()?.message ?: "Register failed")
    }

    override suspend fun getCurrentUser(): Result<User> = runCatching {
        val response = api.getMe()
        if (response.isSuccessful && response.body()?.success == true) {
            val user = response.body()!!.user!!
            encryptedPrefs.edit().putLong(PreferencesKeys.USER_ID, user.id).apply()
            encryptedPrefs.edit().putString(PreferencesKeys.USERNAME, user.username).apply()
            encryptedPrefs.edit().putString(PreferencesKeys.ROLE, user.role).apply()
            user
        } else throw Exception(response.body()?.message ?: "Get user failed")
    }

    override suspend fun logout() { encryptedPrefs.edit().clear().apply() }
    override fun isLoggedIn(): Flow<Boolean> = kotlinx.coroutines.flow.flow { emit(!encryptedPrefs.getString(PreferencesKeys.TOKEN, null).isNullOrEmpty()) }
    override fun getUserRole(): Flow<String?> = kotlinx.coroutines.flow.flow { emit(encryptedPrefs.getString(PreferencesKeys.ROLE, null)) }
}

@Singleton
class AnimeRepositoryImpl @Inject constructor(
    private val api: AniXApi,
    private val animeDao: AnimeDao,
    private val episodeDao: EpisodeDao
) : AnimeRepository {
    override suspend fun getAnimeList(query: String?): Result<List<Anime>> = runCatching {
        val cachedAnime = animeDao.getAllAnime().first()
        if (cachedAnime.isNotEmpty() && query == null) return@runCatching cachedAnime.map { it.toAnime() }
        val response = api.getAnimeList(query)
        if (response.isSuccessful) {
            val list = response.body()?.data ?: emptyList()
            if (query == null) {
                animeDao.clearAll()
                animeDao.insertAllAnime(list.map { it.toEntity() })
            }
            list
        } else throw Exception("Failed to load anime")
    }

    override suspend fun getAnimeById(id: Long): Result<Anime> = runCatching {
        val response = api.getAnimeById(id)
        if (response.isSuccessful) {
            val anime = response.body()?.data ?: throw Exception("Not found")
            animeDao.insertAnime(anime.toEntity())
            anime.episodes.forEach { episodeDao.insertEpisode(it.toEntity(anime.id)) }
            anime
        } else throw Exception("Failed to load anime")
    }

    override suspend fun searchAnime(query: String): Result<List<Anime>> = runCatching {
        val response = api.searchAnime(query)
        if (response.isSuccessful) response.body()?.data ?: emptyList()
        else throw Exception("Search failed")
    }

    override suspend fun getTrending(): Result<List<Anime>> = runCatching {
        val response = api.getTrending()
        if (response.isSuccessful) response.body()?.data ?: emptyList()
        else throw Exception("Failed to load trending")
    }

    override suspend fun getRecommendations(): Result<List<Anime>> = runCatching {
        val response = api.getRecommendations()
        if (response.isSuccessful) response.body()?.data ?: emptyList()
        else throw Exception("Failed to load recommendations")
    }

    override suspend fun getByGenre(genre: String): Result<List<Anime>> = runCatching {
        val response = api.getByGenre(genre)
        if (response.isSuccessful) response.body()?.data ?: emptyList()
        else throw Exception("Failed to load genre")
    }

    override suspend fun getByYear(year: Int): Result<List<Anime>> = runCatching {
        val response = api.getAnimeList()
        if (response.isSuccessful) (response.body()?.data ?: emptyList()).filter { it.year == year }
        else throw Exception("Failed to load by year")
    }

    override suspend fun getRecentlyAdded(): Result<List<Anime>> = runCatching {
        val response = api.getRecentlyAdded()
        if (response.isSuccessful) response.body()?.data ?: emptyList()
        else throw Exception("Failed to load recently added")
    }
}

@Singleton
class WatchRepositoryImpl @Inject constructor(
    private val api: AniXApi,
    private val watchHistoryDao: WatchHistoryDao
) : WatchRepository {
    override suspend fun getWatchHistory(): Result<List<WatchHistoryItem>> = runCatching {
        val response = api.getWatchHistory()
        if (response.isSuccessful) {
            val history = response.body()?.data ?: emptyList()
            watchHistoryDao.clearAllHistory()
            history.forEach { watchHistoryDao.insertHistoryItem(it.toEntity()) }
            history
        } else throw Exception("Failed to load history")
    }

    override suspend fun updateHistory(animeId: Long, episodeNumber: Int, progress: Long, duration: Long, completed: Boolean): Result<Unit> = runCatching {
        api.updateWatchHistory(mapOf("anime_id" to animeId, "episode_number" to episodeNumber, "progress" to progress, "duration" to duration, "completed" to completed))
        Unit
    }

    override suspend fun deleteHistory(animeId: Long): Result<Unit> = runCatching {
        api.deleteWatchHistory(animeId)
        Unit
    }
}

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val api: AniXApi
) : FavoritesRepository {
    override suspend fun getFavorites(): Result<List<FavoriteItem>> = runCatching {
        val response = api.getFavorites()
        if (response.isSuccessful) response.body()?.data ?: emptyList()
        else throw Exception("Failed to load favorites")
    }
    override suspend fun addFavorite(animeId: Long): Result<Unit> = runCatching { api.addFavorite(mapOf("anime_id" to animeId)); Unit }
    override suspend fun deleteFavorite(animeId: Long): Result<Unit> = runCatching { api.deleteFavorite(animeId); Unit }
}

@Singleton
class WatchlistRepositoryImpl @Inject constructor(
    private val api: AniXApi
) : WatchlistRepository {
    // Fix: return List<Anime> sesuai interface dan AniXApi
    override suspend fun getWatchlist(): Result<List<Anime>> = runCatching {
        val response = api.getWatchlist()
        if (response.isSuccessful) response.body()?.data ?: emptyList()
        else throw Exception("Failed to load watchlist")
    }
    override suspend fun addToWatchlist(animeId: Long): Result<Unit> = runCatching { api.addToWatchlist(mapOf("anime_id" to animeId)); Unit }
    override suspend fun removeFromWatchlist(animeId: Long): Result<Unit> = runCatching { api.removeFromWatchlist(animeId); Unit }
}

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val api: AniXApi
) : CommentRepository {
    override suspend fun getComments(animeId: Long): Result<List<Comment>> = runCatching {
        val response = api.getComments(animeId)
        if (response.isSuccessful) response.body()?.data ?: emptyList()
        else throw Exception("Failed to load comments")
    }
    override suspend fun addComment(animeId: Long, content: String, parentId: Long?): Result<Comment> = runCatching {
        val body = mutableMapOf<String, Any>("anime_id" to animeId, "content" to content)
        parentId?.let { body["parent_id"] = it }
        val response = api.addComment(body)
        response.body()?.data ?: throw Exception("Failed to post comment")
    }
    override suspend fun deleteComment(id: Long): Result<Unit> = runCatching { api.deleteComment(id); Unit }
}

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val api: AniXApi,
    private val userProfileDao: UserProfileDao
) : ProfileRepository {
    override suspend fun getProfile(): Result<UserProfile> = runCatching {
        val response = api.getProfile()
        if (response.isSuccessful) {
            val profile = response.body()!!
            userProfileDao.insertUserProfile(profile.toEntity())
            profile
        } else throw Exception("Failed to load profile")
    }
    override suspend fun updateProfile(bio: String?, avatar: String?): Result<Unit> = runCatching {
        api.updateProfile(mutableMapOf<String, String>().apply { 
            bio?.let { put("bio", it) }
            avatar?.let { put("avatar", it) }
        })
        Unit
    }
    override suspend fun uploadAvatar(file: ByteArray): Result<String> = runCatching {
        val part = MultipartBody.Part.createFormData("file", "avatar.jpg", file.toRequestBody("image/*".toMediaTypeOrNull()))
        val response = api.uploadAvatar(part)
        response.body()?.data ?: throw Exception("Upload failed")
    }
}

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val api: AniXApi
) : AdminRepository {
    override suspend fun getAdminAnimeList(): Result<List<Anime>> = runCatching {
        val response = api.getAdminAnimeList()
        if (response.isSuccessful) response.body()?.data ?: emptyList()
        else throw Exception("Failed")
    }
    override suspend fun addAnime(anime: Map<String, Any>): Result<Anime> = runCatching { api.addAnime(anime).body()?.data!! }
    override suspend fun updateAnime(id: Long, anime: Map<String, Any>): Result<Anime> = runCatching { api.updateAnime(id, anime).body()?.data!! }
    override suspend fun deleteAnime(id: Long): Result<Unit> = runCatching { api.deleteAnime(id); Unit }
    override suspend fun getAdminEpisodes(animeId: Long): Result<List<Episode>> = runCatching { api.getAdminEpisodes(animeId).body()?.data ?: emptyList() }
    override suspend fun addEpisode(episode: Map<String, Any>): Result<Episode> = runCatching { api.addEpisode(episode).body()?.data!! }
    override suspend fun deleteEpisode(id: Long): Result<Unit> = runCatching { api.deleteEpisode(id); Unit }
    override suspend fun uploadPoster(file: ByteArray): Result<String> = runCatching {
        val part = MultipartBody.Part.createFormData("file", "poster.jpg", file.toRequestBody("image/*".toMediaTypeOrNull()))
        api.uploadPoster(part).body()?.data!!
    }
    override suspend fun uploadBanner(file: ByteArray): Result<String> = runCatching {
        val part = MultipartBody.Part.createFormData("file", "banner.jpg", file.toRequestBody("image/*".toMediaTypeOrNull()))
        api.uploadBanner(part).body()?.data!!
    }
    override suspend fun uploadVideo(file: ByteArray, animeSlug: String, episodeNumber: Int?): Result<String> = Result.failure(Exception("Not available"))
    override suspend fun getUsers(): Result<List<User>> = runCatching { api.getAdminUsers().body()?.data ?: emptyList() }
    override suspend fun updateUserRole(id: Long, role: String): Result<Unit> = runCatching { api.updateUserRole(id, mapOf("role" to role)); Unit }
    override suspend fun deleteUser(id: Long): Result<Unit> = runCatching { api.deleteUser(id); Unit }
    override suspend fun getStats(): Result<AdminStats> = runCatching { api.getAdminStats().body()?.data!! }
}

// Mappers
fun AnimeEntity.toAnime() = Anime(
    id = id,
    title = title,
    slug = slug,
    alternativeTitle = alternativeTitle,
    synopsis = synopsis,
    thumbnail = thumbnail,
    banner = banner,
    year = year,
    status = status,
    rating = rating,
    ratingCount = ratingCount,
    totalEpisodes = totalEpisodes,
    views = views,
    favorites = favorites,
    malId = malId,
    genres = genres,
    episodes = emptyList()
)

fun Anime.toEntity() = AnimeEntity(
    id = id,
    title = title,
    slug = slug,
    alternativeTitle = alternativeTitle,
    synopsis = synopsis,
    thumbnail = thumbnail,
    banner = banner,
    year = year,
    status = status,
    rating = rating,
    ratingCount = ratingCount,
    totalEpisodes = totalEpisodes,
    views = views,
    favorites = favorites,
    malId = malId,
    genres = genres,
    createdAt = ""
)

fun EpisodeEntity.toEpisode() = Episode(id, animeId, episodeNumber, title, videoUrl, thumbnail, subtitleUrl, durationSec)
fun Episode.toEntity(animeId: Long) = EpisodeEntity(id, animeId, episodeNumber, title, videoUrl, thumbnail, subtitleUrl, durationSec)
fun UserProfileEntity.toUserProfile() = UserProfile(id, username, email, role, avatar, bio, createdAt ?: "", null)
fun UserProfile.toEntity() = UserProfileEntity(id, username, email, role, createdAt, avatar, bio)
// Fix: WatchHistoryItem sekarang punya id, userId, episodeNumber: Int
fun WatchHistoryEntity.toWatchHistoryItem() = WatchHistoryItem(id, userId, animeId, "", "", null, episodeNumber, progress, duration, completed, lastWatched)
fun WatchHistoryItem.toEntity() = WatchHistoryEntity(0, userId, animeId, episodeNumber, progress, duration, completed, lastWatched)
