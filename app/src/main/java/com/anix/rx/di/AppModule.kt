package com.anix.rx.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.anix.rx.BuildConfig
import com.anix.rx.data.api.AniXApi
import com.anix.rx.data.api.AuthInterceptor
import com.anix.rx.ui.theme.dataStore
import com.anix.rx.data.local.AnixDatabase
import com.anix.rx.data.local.dao.AnimeDao
import com.anix.rx.data.local.dao.EpisodeDao
import com.anix.rx.data.local.dao.UserProfileDao
import com.anix.rx.data.local.dao.WatchHistoryDao
import com.anix.rx.data.repository.*
import com.anix.rx.domain.repository.*
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        json: Json
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL + "/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideAniXApi(retrofit: Retrofit): AniXApi {
        return retrofit.create(AniXApi::class.java)
    }
    
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }

    // --- Database Section ---

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AnixDatabase {
        return Room.databaseBuilder(
            context,
            AnixDatabase::class.java,
            AnixDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideAnimeDao(database: AnixDatabase): AnimeDao = database.animeDao()

    @Provides
    @Singleton
    fun provideEpisodeDao(database: AnixDatabase): EpisodeDao = database.episodeDao()

    @Provides
    @Singleton
    fun provideUserProfileDao(database: AnixDatabase): UserProfileDao = database.userProfileDao()

    @Provides
    @Singleton
    fun provideWatchHistoryDao(database: AnixDatabase): WatchHistoryDao = database.watchHistoryDao()

    // --- Repositories Section ---

    @Provides
    @Singleton
    fun provideAuthRepository(
        api: AniXApi,
        @ApplicationContext context: Context
    ): AuthRepository = AuthRepositoryImpl(api, context)

    @Provides
    @Singleton
    fun provideAnimeRepository(
        api: AniXApi,
        animeDao: AnimeDao,
        episodeDao: EpisodeDao
    ): AnimeRepository = AnimeRepositoryImpl(api, animeDao, episodeDao)

    @Provides
    @Singleton
    fun provideWatchRepository(
        api: AniXApi,
        watchHistoryDao: WatchHistoryDao
    ): WatchRepository = WatchRepositoryImpl(api, watchHistoryDao)

    @Provides
    @Singleton
    fun provideFavoritesRepository(
        api: AniXApi
    ): FavoritesRepository = FavoritesRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideWatchlistRepository(
        api: AniXApi
    ): WatchlistRepository = WatchlistRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideCommentRepository(
        api: AniXApi
    ): CommentRepository = CommentRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideProfileRepository(
        api: AniXApi,
        userProfileDao: UserProfileDao
    ): ProfileRepository = ProfileRepositoryImpl(api, userProfileDao)

    @Provides
    @Singleton
    fun provideAdminRepository(
        api: AniXApi
    ): AdminRepository = AdminRepositoryImpl(api)
}
