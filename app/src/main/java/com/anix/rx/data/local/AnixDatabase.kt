package com.anix.rx.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.anix.rx.data.local.dao.*
import com.anix.rx.data.local.entity.*

@Database(
    entities = [AnimeEntity::class, EpisodeEntity::class, UserProfileEntity::class, WatchHistoryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AnixDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun watchHistoryDao(): WatchHistoryDao

    companion object {
        const val DATABASE_NAME = "anix_db"
    }
}
