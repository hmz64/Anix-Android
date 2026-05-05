package com.anix.rx.domain.repository

import com.anix.rx.data.model.*
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthResponse>
    suspend fun register(username: String, email: String, password: String): Result<AuthResponse>
    suspend fun getCurrentUser(): Result<User>
    suspend fun logout()
    fun isLoggedIn(): Flow<Boolean>
    fun getUserRole(): Flow<String?>
}

interface AnimeRepository {
    suspend fun getAnimeList(query: String? = null): Result<List<Anime>>
    suspend fun getAnimeById(id: Long): Result<Anime>
    suspend fun searchAnime(query: String): Result<List<Anime>>
    suspend fun getTrending(): Result<List<Anime>>
    suspend fun getRecommendations(): Result<List<Anime>>
    suspend fun getByGenre(genre: String): Result<List<Anime>>
    suspend fun getByYear(year: Int): Result<List<Anime>>
    suspend fun getRecentlyAdded(): Result<List<Anime>>
}

interface WatchRepository {
    suspend fun getWatchHistory(): Result<List<WatchHistoryItem>>
    suspend fun updateHistory(
        animeId: Long,
        episodeNumber: Int,
        progress: Long,
        duration: Long,
        completed: Boolean
    ): Result<Unit>
    suspend fun deleteHistory(animeId: Long): Result<Unit>
}

interface FavoritesRepository {
    suspend fun getFavorites(): Result<List<FavoriteItem>>
    suspend fun addFavorite(animeId: Long): Result<Unit>
    suspend fun deleteFavorite(animeId: Long): Result<Unit>
}

// Fix: WatchlistRepository return type harus List<Anime> (konsisten dengan AniXApi)
interface WatchlistRepository {
    suspend fun getWatchlist(): Result<List<Anime>>
    suspend fun addToWatchlist(animeId: Long): Result<Unit>
    suspend fun removeFromWatchlist(animeId: Long): Result<Unit>
}

interface CommentRepository {
    suspend fun getComments(animeId: Long): Result<List<Comment>>
    suspend fun addComment(animeId: Long, content: String, parentId: Long? = null): Result<Comment>
    suspend fun deleteComment(id: Long): Result<Unit>
}

interface ProfileRepository {
    suspend fun getProfile(): Result<UserProfile>
    suspend fun updateProfile(bio: String? = null, avatar: String? = null): Result<Unit>
    suspend fun uploadAvatar(file: ByteArray): Result<String>
}

interface AdminRepository {
    suspend fun getAdminAnimeList(): Result<List<Anime>>
    suspend fun addAnime(anime: Map<String, Any>): Result<Anime>
    suspend fun updateAnime(id: Long, anime: Map<String, Any>): Result<Anime>
    suspend fun deleteAnime(id: Long): Result<Unit>

    suspend fun getAdminEpisodes(animeId: Long): Result<List<Episode>>
    suspend fun addEpisode(episode: Map<String, Any>): Result<Episode>
    suspend fun deleteEpisode(id: Long): Result<Unit>

    suspend fun uploadPoster(file: ByteArray): Result<String>
    suspend fun uploadBanner(file: ByteArray): Result<String>
    suspend fun uploadVideo(file: ByteArray, animeSlug: String, episodeNumber: Int?): Result<String>

    suspend fun getUsers(): Result<List<User>>
    suspend fun updateUserRole(id: Long, role: String): Result<Unit>
    suspend fun deleteUser(id: Long): Result<Unit>

    suspend fun getStats(): Result<AdminStats>
}
