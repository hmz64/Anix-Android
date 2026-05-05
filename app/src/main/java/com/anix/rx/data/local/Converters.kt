package com.anix.rx.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class Converters {
    // Inisialisasi Json dengan konfigurasi agar mengabaikan field yang tidak dikenal
    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }

    @TypeConverter
    fun fromGenreList(genres: List<String>?): String {
        return json.encodeToString(genres ?: emptyList())
    }

    @TypeConverter
    fun toGenreList(genresString: String?): List<String> {
        return try {
            if (genresString.isNullOrEmpty()) {
                emptyList()
            } else {
                json.decodeFromString(genresString)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromBoolean(value: Boolean): Int {
        return if (value) 1 else 0
    }

    @TypeConverter
    fun toBoolean(value: Int): Boolean {
        return value == 1
    }
}
