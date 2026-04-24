package com.anix.rx.data.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "anix_prefs")

@Singleton
class AuthInterceptor @Inject constructor(
    @ApplicationContext private val context: Context
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip auth for login/register endpoints
        if (originalRequest.url.encodedPath.contains("/api/login") || 
            originalRequest.url.encodedPath.contains("/api/register")) {
            return chain.proceed(originalRequest)
        }
        
        val token = runBlocking {
            try {
                context.dataStore.data.first()[PreferencesKeys.TOKEN]
            } catch (e: Exception) {
                null
            }
        }
        
        return if (token != null) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}

object PreferencesKeys {
    val TOKEN = androidx.datastore.preferences.core.stringPreferencesKey("token")
    val REFRESH_TOKEN = androidx.datastore.preferences.core.stringPreferencesKey("refresh_token")
    val USER_ID = androidx.datastore.preferences.core.longPreferencesKey("user_id")
    val USERNAME = androidx.datastore.preferences.core.stringPreferencesKey("username")
    val ROLE = androidx.datastore.preferences.core.stringPreferencesKey("role")
}