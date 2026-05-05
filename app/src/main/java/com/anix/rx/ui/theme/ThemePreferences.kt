package com.anix.rx.ui.theme

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "anix_prefs")

object ThemePreferences {
    val THEME_MODE = stringPreferencesKey("theme_mode")
    
    suspend fun saveThemeMode(context: Context, mode: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME_MODE] = mode
        }
    }
    
    fun getThemeMode(context: Context) = context.dataStore.data.map { prefs ->
        prefs[THEME_MODE] ?: "system" // "system", "light", "dark"
    }
}
