package com.anix.rx.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.anix.rx.data.local.entity.AnimeEntity
import com.anix.rx.data.local.entity.EpisodeEntity
import com.anix.rx.data.local.entity.UserProfileEntity
import com.anix.rx.data.local.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnimeDao {
    @Query("SELECT * FROM anime ORDER BY createdAt DESC")
    fun getAllAnime(): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime WHERE id = :id")
    suspend fun getAnimeById(id: Long): AnimeEntity?

    @Query("SELECT * FROM anime WHERE title LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchAnime(query: String): Flow<List<AnimeEntity>>

    @Query("SELECT * FROM anime WHERE status = :status ORDER BY createdAt DESC")
    fun getAnimeByStatus(status: String): Flow<List<AnimeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnime(anime: AnimeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllAnime(anime: List<AnimeEntity>)

    @Update
    suspend fun updateAnime(anime: AnimeEntity)

    @Query("DELETE FROM anime WHERE id = :id")
    suspend fun deleteAnime(id: Long)

    @Query("DELETE FROM anime")
    suspend fun clearAll()
}

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episodes WHERE animeId = :animeId ORDER BY episodeNumber")
    suspend fun getEpisodesByAnimeId(animeId: Long): List<EpisodeEntity>

    @Query("SELECT * FROM episodes WHERE animeId = :animeId AND episodeNumber = :episodeNumber")
    suspend fun getEpisode(animeId: Long, episodeNumber: Int): EpisodeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(episode: EpisodeEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllEpisodes(episodes: List<EpisodeEntity>)

    @Update
    suspend fun updateEpisode(episode: EpisodeEntity)

    @Query("DELETE FROM episodes WHERE animeId = :animeId")
    suspend fun deleteEpisodesByAnimeId(animeId: Long)

    @Query("DELETE FROM episodes WHERE id = :id")
    suspend fun deleteEpisode(id: Long)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = :id")
    suspend fun getUserProfile(id: Long): UserProfileEntity?

    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getAllProfiles(): Flow<List<UserProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(user: UserProfileEntity)

    @Update
    suspend fun updateUserProfile(user: UserProfileEntity)

    @Query("DELETE FROM user_profile")
    suspend fun clearUserProfile()
}

@Dao
interface WatchHistoryDao {
    @Query("SELECT * FROM watch_history ORDER BY lastWatched DESC")
    fun getAllHistory(): Flow<List<WatchHistoryEntity>>

    @Query("SELECT * FROM watch_history WHERE animeId = :animeId AND episodeNumber = :episodeNumber")
    suspend fun getHistoryItem(animeId: Long, episodeNumber: Int): WatchHistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryItem(item: WatchHistoryEntity)

    @Update
    suspend fun updateHistoryItem(item: WatchHistoryEntity)

    @Query("DELETE FROM watch_history WHERE animeId = :animeId")
    suspend fun deleteHistoryByAnimeId(animeId: Long)

    @Query("DELETE FROM watch_history")
    suspend fun clearAllHistory()
}
