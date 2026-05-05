package com.anix.rx

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.anix.rx.ui.AniXApp
import com.anix.rx.ui.theme.AniXTheme
import dagger.hilt.android.AndroidEntryPoint

data class DeepLinkData(
    val animeId: Long? = null,
    val episodeNumber: Int? = null
)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val deepLink = parseDeepLink(intent)

        setContent {
            AniXTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AniXApp(initialDeepLink = deepLink)
                }
            }
        }
    }

    // Handle deep link when app is already running (e.g. from notification)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // Re-parse and navigate — handled via AniXApp recomposition via intent state
    }

    private fun parseDeepLink(intent: Intent?): DeepLinkData? {
        if (intent?.action != Intent.ACTION_VIEW) return null
        val uri: Uri = intent.data ?: return null

        return when (uri.scheme) {
            // anix://anime/123
            "anix" -> when (uri.host) {
                "anime" -> {
                    val animeId = uri.pathSegments.firstOrNull()?.toLongOrNull()
                    if (animeId != null) DeepLinkData(animeId = animeId) else null
                }
                // anix://watch/123/1  (animeId/episodeNumber)
                "watch" -> {
                    val animeId = uri.pathSegments.getOrNull(0)?.toLongOrNull()
                    val ep = uri.pathSegments.getOrNull(1)?.toIntOrNull()
                    if (animeId != null) DeepLinkData(animeId = animeId, episodeNumber = ep) else null
                }
                else -> null
            }
            // https://anix.app/anime/123
            "https", "http" -> {
                val segments = uri.pathSegments
                when {
                    segments.size >= 2 && segments[0] == "anime" -> {
                        val animeId = segments[1].toLongOrNull()
                        if (animeId != null) DeepLinkData(animeId = animeId) else null
                    }
                    // https://anix.app/watch/123/1
                    segments.size >= 3 && segments[0] == "watch" -> {
                        val animeId = segments[1].toLongOrNull()
                        val ep = segments[2].toIntOrNull()
                        if (animeId != null) DeepLinkData(animeId = animeId, episodeNumber = ep) else null
                    }
                    else -> null
                }
            }
            else -> null
        }
    }
}
