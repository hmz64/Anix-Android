package com.anix.rx.data.api

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.anix.rx.data.api.PreferencesKeys.TOKEN
import com.anix.rx.data.api.PreferencesKeys.REFRESH_TOKEN
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

// Use EncryptedSharedPreferences instead of regular DataStore for sensitive tokens
fun getEncryptedPrefs(context: Context) = 
    EncryptedSharedPreferences.create(
        context,
        "anix_secure_prefs",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

// Fix: Hapus AniXApi dari constructor AuthInterceptor untuk menghindari circular dependency.
// Token refresh dilakukan dengan OkHttpClient baru (tanpa interceptor ini).
@Singleton
class AuthInterceptor @Inject constructor(
    @ApplicationContext private val context: Context
) : Interceptor {
    
    private val encryptedPrefs = getEncryptedPrefs(context)
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip auth for login/register endpoints
        if (originalRequest.url.encodedPath.contains("/api/login") || 
            originalRequest.url.encodedPath.contains("/api/register")) {
            return chain.proceed(originalRequest)
        }
        
        val token = encryptedPrefs.getString(TOKEN, null)
        
        val response = if (token != null) {
            val newRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
        
        // Auto-refresh token on 401
        if (response.code == 401) {
            val refreshToken = encryptedPrefs.getString(REFRESH_TOKEN, null)
            if (refreshToken != null) {
                try {
                    // Use a plain OkHttpClient (no interceptor) to avoid circular call
                    val refreshClient = OkHttpClient()
                    val baseUrl = originalRequest.url.scheme + "://" + originalRequest.url.host +
                        if (originalRequest.url.port != -1) ":${originalRequest.url.port}" else ""
                    val refreshRequest = okhttp3.Request.Builder()
                        .url("$baseUrl/api/refresh")
                        .post(
                            okhttp3.RequestBody.create(
                                "application/json".toMediaType(),
                                """{"refresh_token":"$refreshToken"}"""
                            )
                        )
                        .build()
                    val refreshResponse = refreshClient.newCall(refreshRequest).execute()
                    if (refreshResponse.isSuccessful) {
                        val body = refreshResponse.body?.string()
                        // Parse token manually (simple JSON extraction)
                        val newToken = body?.let {
                            Regex(""""token"\s*:\s*"([^"]+)"""").find(it)?.groupValues?.getOrNull(1)
                        }
                        if (newToken != null) {
                            encryptedPrefs.edit().putString(TOKEN, newToken).apply()
                            val retryRequest = originalRequest.newBuilder()
                                .header("Authorization", "Bearer $newToken")
                                .build()
                            return chain.proceed(retryRequest)
                        }
                    }
                } catch (e: Exception) {
                    // Refresh failed, return original 401 response
                }
            }
        }
        
        return response
    }
}

object PreferencesKeys {
    const val TOKEN = "token"
    const val REFRESH_TOKEN = "refresh_token"
    const val USER_ID = "user_id"
    const val USERNAME = "username"
    const val ROLE = "role"
}
