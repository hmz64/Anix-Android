# ProGuard rules for ANIX Android app

# Keep ANIX-specific classes
-keep class com.anix.rx.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# Keep Compose
-keep class androidx.compose.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Keep Retrofit/OkHttp
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn okhttp3.**

# Keep kotlinx serialization
-keepattributes *Annotation*, InnerClasses
-keep class kotlinx.serialization.** { *; }

# Keep DataStore/EncryptedSharedPreferences
-keep class androidx.datastore.** { *; }
-keep class androidx.security.crypto.** { *; }
-keep class com.google.crypto.tink.** { *; }

# Keep ExoPlayer/Media3
-keep class androidx.media3.** { *; }
-keep class com.google.android.exoplayer2.** { *; }

# Keep Coil
-keep class coil.** { *; }

# General
-dontwarn kotlinx.**
-dontwarn androidx.**
