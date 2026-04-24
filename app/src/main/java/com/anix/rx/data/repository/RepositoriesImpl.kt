package com.anix.rx.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.anix.rx.data.api.AniXApi
import com.anix.rx.data.api.PreferencesKeys
import com.anix.rx.data.model.*
import com.anix.rx.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val api: AniXApi,
    private val dataStore: DataStore<Preferences>
) : AuthRepository {
    
    override suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.login(buildJsonObject {
                put("email", email)
                put("password", password)
            }.toMap().mapValues { it.value.toString() })
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.let { auth ->
                    auth.token?.let { token ->
                        dataStore[PreferencesKeys.TOKEN] = token
                    }
                    auth.refreshToken?.let { token ->
                        dataStore[PreferencesKeys.REFRESH_TOKEN] = token
                    }
                    auth.user?.let { user ->
                        dataStore[PreferencesKeys.USER_ID] = user.id
                        dataStore[PreferencesKeys.USERNAME] = user.username
                        dataStore[PreferencesKeys.ROLE] = user.role
                    }
                }
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun register(username: String, email: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.register(buildJsonObject {
                put("username", username)
                put("email", email)
                put("password", password)
            }.toMap().mapValues { it.value.toString() })
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.let { auth ->
                    auth.token?.let { token ->
                        dataStore[PreferencesKeys.TOKEN] = token
                    }
                }
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Register failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getCurrentUser(): Result<User> {
        return try {
            val response = api.getMe()
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.user?.let { user ->
                    dataStore[PreferencesKeys.USER_ID] = user.id
                    dataStore[PreferencesKeys.USERNAME] = user.username
                    dataStore[PreferencesKeys.ROLE] = user.role
                }
                Result.success(response.body()!!.user!!)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Get user failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun logout() {
        dataStore[PreferencesKeys.TOKEN] = ""
        dataStore[PreferencesKeys.REFRESH_TOKEN] = ""
        dataStore[PreferencesKeys.USER_ID] = 0L
        dataStore[PreferencesKeys.USERNAME] = ""
        dataStore[PreferencesKeys.ROLE] = ""
    }
    
    override fun isLoggedIn(): Flow<Boolean> = dataStore.data.map { prefs ->
        !prefs[PreferencesKeys.TOKEN].isNullOrEmpty()
    }
    
    override fun getUserRole(): Flow<String?> = dataStore.data.map { prefs ->
        prefs[PreferencesKeys.ROLE]
    }
}

@Singleton
class AnimeRepositoryImpl @Inject constructor(
    private val api: AniXApi
) : AnimeRepository {
    
    override suspend fun getAnimeList(): Result<List<Anime>> = runCatching {
        val response = api.getAnimeList()
        if (response.isSuccessful) response.body()?.data ?: emptyList()
        else throw Exception(response.body()?.message)
    }
    
    override suspend fun getAnimeById(id: Long): Result<Anime> = runCatching {
        val response = api.getAnimeById(id)
        if (response.isSuccessful) response.body()?.data!!
        else throw Exception(response.body()?.message)
    }
    
    override suspend fun searchAnime(query: String): Result<List<Anime>> = runCatching {
        val response = api.searchAnime(query)
        if (response.isSuccessful) response.body()?.data ?: emptyList()
        else throw Exception(response.body()?.message)
    }
    
    override suspend fun getTrending(): Result<List<Anime>> = runCatching {
        val response = api.getTrending()
        if (response.isSuccessful) response.body()?.data ?: emptyList()
        else throw Exception(response.body()?.message)
    }
    
    override suspend fun getRecommendations(): Result<List<Anime>> = runCatching {
        val response = api.getRecommendations()
        if (response.isSuccessful) response.body()?.data ?: emptyList()
        else throw Exception(response.body()?.message)
    }
    
    override suspend fun getByGenre(genre: String): Result<List<Anime>> = runCatching {
        val response = api.getByGenre(genre)
        if (response.isSuccessful) response.body()?.data ?: emptyList()
        else throw Exception(response.body()?.message)
    }
    
    override suspend fun getByYear(year: Int): Result<List<Anime>> = runCatching {
        val response = api.getByYear(year)
        if (response.isSuccessful) response.body()?.data ?: emptyList()
        else throw Exception(response.body()?.message)
    }
    
    override suspend fun getRecentlyAdded(): Result<List<Anime>> = runCatching {
        val response = api.getRecentlyAdded()
        if (response.isSuccessful) response.body()?.data ?: emptyList()
        else throw Exception(response.body()?.message)
    }
}

@Singleton
class WatchRepositoryImpl @Inject constructor(
    private val api: AniXApi
) : WatchRepository {
    
    override suspend fun getWatchHistory(): Result<List<WatchHistoryItem>> = runCatching {
        val response = api.getWatchHistory()
        if (response.isSuccessful) response.body()?.data ?: emptyList()
        else throw Exception(response.body()?.message)
    }
    
    override suspend fun updateHistory(animeId: Long, episodeNumber: Int, progress: Long, duration: Long, completed: Boolean): Result<Unit> = runCatching {
        val response = api.updateWatchHistory(mapOf(
            "anime_id" to animeId,
            "episode_number" to episodeNumber,
            "progress" to progress,
            "duration" to duration,
            "completed" to if (completed) 1 else 0
        ))
        if (!response.isSuccessful) throw Exception(response.body()?.message)
    }
    
    override suspend fun deleteHistory(animeId: Long): Result<Unit> = runCatching {
        val response = api.deleteWatchHistory(animeId)
        if (!response.isSuccessful) throw Exception(response.body()?.message)
    }
}

@Singleton
class FavoritesRepositoryImpl @Inject constructor(
    private val api: AniXApi
) : FavoritesRepository {
    
    override suspend fun getFavorites(): Result<List<FavoriteItem>> = runCatching {
        val response = api.getFavorites()
        if (response.isSuccessful) response.body()?.data ?: emptyList()
        else throw Exception(response.body()?.message)
    }
    
    override suspend fun addFavorite(animeId: Long): Result<Unit> = runCatching {
        val response = api.addFavorite(mapOf("anime_id" to animeId))
        if (!response.isSuccessful) throw Exception(response.body()?.message)
    }
    
    override suspend fun deleteFavorite(animeId: Long): Result<Unit> = runCatching {
        val response = api.deleteFavorite(animeId)
        if (!response.isSuccessful) throw Exception(response.body()?.message)
    }
}

@Singleton
class WatchlistRepositoryImpl @Inject constructor(
    private val api: AniXApi
) : WatchlistRepository {
    
    override suspend fun getWatchlist(): Result<List<Anime>> = runCatching {
        val response = api.getWatchlist()
        if (response.isSuccessful) response.body()?.data ?: emptyList()
        else throw Exception(response.body()?.message)
    }
    
    override suspend fun addToWatchlist(animeId: Long): Result<Unit> = runCatching {
        val response = api.addToWatchlist(mapOf("anime_id" to animeId))
        if (!response.isSuccessful) throw Exception(response.body()?.message)
    }
    
    override suspend fun removeFromWatchlist(animeId: Long): Result<Unit> = runCatching {
        val response = api.removeFromWatchlist(animeId)
        if (!response.isSuccessful) throw Exception(response.body()?.message)
    }
}

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val api: AniXApi
) : CommentRepository {
    
    override suspend fun getComments(animeId: Long): Result<List<Comment>> = runCatching {
        val response = api.getComments(animeId)
        if (response.isSuccessful) response.body()?.data ?: emptyList()
        else throw Exception(response.body()?.message)
    }
    
    override suspend fun addComment(animeId: Long, content: String, episodeNumber: Int?): Result<Comment> = runCatching {
        val body = buildMap {
            put("anime_id", animeId)
            put("content", content)
            episodeNumber?.let { put("episode_number", it) }
        }
        val response = api.addComment(body)
        if (response.isSuccessful) response.body()?.data!!
        else throw Exception(response.body()?.message)
    }
    
    override suspend fun deleteComment(id: Long): Result<Unit> = runCatching {
        val response = api.deleteComment(id)
        if (!response.isSuccessful) throw Exception(response.body()?.message)
    }
}

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val api: AniXApi
) : ProfileRepository {
    
    override suspend fun getProfile(): Result<UserProfile> = runCatching {
        val response = api.getProfile()
        if (response.isSuccessful) response.body()?.data!!
        else throw Exception(response.body()?.message)
    }
    
    override suspend fun updateProfile(bio: String?, avatar: String?): Result<User> = runCatching {
        val body = buildMapOf<String, String>()
        bio?.let { body["bio"] = it }
        avatar?.let { body["avatar"] = it }
        val response = api.updateProfile(body)
        if (response.isSuccessful) response.body()?.data!!
        else throw Exception(response.body()?.message)
    }
    
    override suspend fun uploadAvatar(file: ByteArray): Result<String> = runCatching {
        // Implementation for file upload
        throw Exception("Not implemented")
    }
}

@Singleton
class AdminRepositoryImpl @Inject constructor(
    private val api: AniXApi
) : AdminRepository {
    
    override suspend fun getAdminAnimeList(): Result<List<Anime>> = runCatching {
        val response = api.getAdminAnimeList()
        if (response.isSuccessful) response.body()?.data ?: emptyList()
        else throw Exception(response.body()?.message)
    }
    
    override suspend fun addAnime(anime: Map<String, Any>): Result<Anime> = runCatching {
        val response = api.addAnime(anime)
        if (response.isSuccessful) response.body()?.data!!
        else throw Exception(response.body()?.message)
    }
    
    override suspend fun updateAnime(id: Long, anime: Map<String, Any>): Result<Anime> = runCatching {
        val response = api.updateAnime(id, anime)
        if (response.isSuccessful) response.body()?.data!!
        else throw Exception(response.body()?.message)
    }
    
    override suspend fun deleteAnime(id: Long): Result<Unit> = runCatching {
        val response = api.deleteAnime(id)
        if (!response.isSuccessful) throw Exception(response.body()?.message)
    }
    
    override suspend fun getAdminEpisodes(animeId: Long): Result<List<Episode>> = runCatching {
        val response = api.getAdminEpisodes(animeId)
        if (response.isSuccessful) response.body()?.data ?: emptyList()
        else throw Exception(response.body()?.message)
    }
    
    override suspend fun addEpisode(episode: Map<String, Any>): Result<Episode> = runCatching {
        val response = api.addEpisode(episode)
        if (response.isSuccessful) response.body()?.data!!
        else throw Exception(response.body()?.message)
    }
    
    override suspend fun deleteEpisode(id: Long): Result<Unit> = runCatching {
        val response = api.deleteEpisode(id)
        if (!response.isSuccessful) throw Exception(response.body()?.message)
    }
    
    override suspend fun uploadPoster(file: ByteArray): Result<String> = runCatching {
        throw Exception("Not implemented")
    }
    
    override suspend fun uploadBanner(file: ByteArray): Result<String> = runCatching {
        throw Exception("Not implemented")
    }
    
    override suspend fun uploadVideo(file: ByteArray, animeSlug: String, episodeNumber: Int?): Result<String> = runCatching {
        throw Exception("Not implemented")
    }
    
    override suspend fun getUsers(): Result<List<User>> = runCatching {
        val response = api.getAdminUsers()
        if (response.isSuccessful) response.body()?.data ?: emptyList()
        else throw Exception(response.body()?.message)
    }
    
    override suspend fun updateUserRole(id: Long, role: String): Result<Unit> = runCatching {
        val response = api.updateUserRole(id, mapOf("role" to role))
        if (!response.isSuccessful) throw Exception(response.body()?.message)
    }
    
    override suspend fun deleteUser(id: Long): Result<Unit> = runCatching {
        val response = api.deleteUser(id)
        if (!response.isSuccessful) throw Exception(response.body()?.message)
    }
    
    override suspend fun getStats(): Result<AdminStats> = runCatching {
        val response = api.getAdminStats()
        if (response.isSuccessful) response.body()?.data!!
        else throw Exception(response.body()?.message)
    }
}