package com.anix.rx.data.api

import com.anix.rx.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface AniXApi {

    // Auth
    @POST("api/register")
    suspend fun register(@Body body: Map<String, String>): Response<AuthResponse>

    @POST("api/login")
    suspend fun login(@Body body: Map<String, String>): Response<AuthResponse>

    @GET("api/me")
    suspend fun getMe(): Response<AuthResponse>

    @POST("api/refresh")
    suspend fun refreshToken(): Response<AuthResponse>

    // Anime API
    @GET("api/anime")
    suspend fun getAnimeList(): Response<ApiResponse<List<Anime>>>>

    @GET("api/anime/{id}")
    suspend fun getAnimeById(@Path("id") id: Long): Response<ApiResponse<Anime>>>

    @GET("api/anime/search")
    suspend fun searchAnime(@Query("q") query: String): Response<ApiResponse<List<Anime>>>>

    @GET("api/trending")
    suspend fun getTrending(): Response<ApiResponse<List<Anime>>>>

    @GET("api/recommendations")
    suspend fun getRecommendations(): Response<ApiResponse<List<Anime>>>>

    @GET("api/anime/by-genre/{genre}")
    suspend fun getByGenre(@Path("genre") genre: String): Response<ApiResponse<List<Anime>>>>

    @GET("api/anime/by-year/{year}")
    suspend fun getByYear(@Path("year") year: Int): Response<ApiResponse<List<Anime>>>>

    @GET("api/recently-added")
    suspend fun getRecentlyAdded(): Response<ApiResponse<List<Anime>>>>

    // Watch History
    @GET("api/watch-history")
    suspend fun getWatchHistory(): Response<ApiResponse<List<WatchHistoryItem>>>>

    @POST("api/watch-history")
    suspend fun updateWatchHistory(@Body body: Map<String, Any>): Response<ApiResponse<Unit>>

    @DELETE("api/watch-history/{animeId}")
    suspend fun deleteWatchHistory(@Path("animeId") animeId: Long): Response<ApiResponse<Unit>>

    // Favorites
    @GET("api/favorites")
    suspend fun getFavorites(): Response<ApiResponse<List<FavoriteItem>>>>

    @POST("api/favorites")
    suspend fun addFavorite(@Body body: Map<String, Long>): Response<ApiResponse<Unit>>

    @DELETE("api/favorites/{animeId}")
    suspend fun deleteFavorite(@Path("animeId") animeId: Long): Response<ApiResponse<Unit>>

    // Watchlist
    @GET("api/watchlist")
    suspend fun getWatchlist(): Response<ApiResponse<List<Anime>>>>

    @POST("api/watchlist")
    suspend fun addToWatchlist(@Body body: Map<String, Long>): Response<ApiResponse<Unit>>

    @DELETE("api/watchlist/{animeId}")
    suspend fun removeFromWatchlist(@Path("animeId") animeId: Long): Response<ApiResponse<Unit>>

    // Ratings
    @POST("api/ratings")
    suspend fun addRating(@Body body: Map<String, Any>): Response<ApiResponse<Unit>>

    @GET("api/ratings/{animeId}")
    suspend fun getRating(@Path("animeId") animeId: Long): Response<ApiResponse<Rating>>>>

    // Comments
    @GET("api/comments/{animeId}")
    suspend fun getComments(@Path("animeId") animeId: Long): Response<ApiResponse<List<Comment>>>>

    @POST("api/comments")
    suspend fun addComment(@Body body: Map<String, Any>): Response<ApiResponse<Comment>>>>

    @DELETE("api/comments/{id}")
    suspend fun deleteComment(@Path("id") id: Long): Response<ApiResponse<Unit>>

    // User Profile
    @GET("api/user/profile")
    suspend fun getProfile(): Response<ApiResponse<UserProfile>>>>

    @PUT("api/user/profile")
    suspend fun updateProfile(@Body body: Map<String, String>): Response<ApiResponse<User>>>>

    @Multipart
    @POST("api/user/avatar")
    suspend fun uploadAvatar(@Part file: MultipartBody.Part): Response<ApiResponse<String>>>>

    // Notifications
    @GET("api/notifications")
    suspend fun getNotifications(): Response<ApiResponse<List<Notification>>>>

    @POST("api/notifications/read/{id}")
    suspend fun markNotificationRead(@Path("id") id: Long): Response<ApiResponse<Unit>>

    @POST("api/notifications/read-all")
    suspend fun markAllNotificationsRead(): Response<ApiResponse<Unit>>

    // Upload
    @Multipart
    @POST("api/admin/upload/poster")
    suspend fun uploadPoster(@Part file: MultipartBody.Part): Response<ApiResponse<String>>>>

    @Multipart
    @POST("api/admin/upload/banner")
    suspend fun uploadBanner(@Part file: MultipartBody.Part): Response<ApiResponse<String>>>>

    @Multipart
    @POST("api/admin/upload/video")
    suspend fun uploadVideo(
        @Part file: MultipartBody.Part,
        @Query("anime_slug") animeSlug: String,
        @Query("episode_number") episodeNumber: Int?
    ): Response<ApiResponse<String>>>>

    // Admin Anime
    @GET("api/admin/anime")
    suspend fun getAdminAnimeList(): Response<ApiResponse<List<Anime>>>>

    @POST("api/admin/anime")
    suspend fun addAnime(@Body body: Map<String, Any>): Response<ApiResponse<Anime>>>>

    @PUT("api/admin/anime/{id}")
    suspend fun updateAnime(
        @Path("id") id: Long,
        @Body body: Map<String, Any>
    ): Response<ApiResponse<Anime>>>>

    @DELETE("api/admin/anime/{id}")
    suspend fun deleteAnime(@Path("id") id: Long): Response<ApiResponse<Unit>>

    // Episodes
    @GET("api/admin/episodes/{animeId}")
    suspend fun getAdminEpisodes(@Path("animeId") animeId: Long): Response<ApiResponse<List<Episode>>>>

    @POST("api/admin/episodes")
    suspend fun addEpisode(@Body body: Map<String, Any>): Response<ApiResponse<Episode>>>>

    @DELETE("api/admin/episodes/{id}")
    suspend fun deleteEpisode(@Path("id") id: Long): Response<ApiResponse<Unit>>

    // Users
    @GET("api/admin/users")
    suspend fun getAdminUsers(): Response<ApiResponse<List<User>>>>

    @PUT("api/admin/users/{id}/role")
    suspend fun updateUserRole(
        @Path("id") id: Long,
        @Body body: Map<String, String>
    ): Response<ApiResponse<Unit>>

    @DELETE("api/admin/users/{id}")
    suspend fun deleteUser(@Path("id") id: Long): Response<ApiResponse<Unit>>

    // Stats
    @GET("api/admin/stats")
    suspend fun getAdminStats(): Response<ApiResponse<AdminStats>>>
}