interface WatchlistRepository {
    suspend fun getWatchlist(): Result<List<WatchlistItem>>
    suspend fun addToWatchlist(animeId: Long): Result<Unit>
    suspend fun removeFromWatchlist(animeId: Long): Result<Unit>
}
