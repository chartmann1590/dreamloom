# Hilt / Dagger generated code
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel
-keepclasseswithmembers class * { @dagger.hilt.android.lifecycle.HiltViewModel <init>(...); }
-dontwarn dagger.internal.codegen.**

# LiteRT-LM (model engine)
-keep class com.google.ai.edge.litert.** { *; }
-keep class com.google.ai.edge.litertlm.** { *; }
-dontwarn com.google.ai.edge.litert.**
-dontwarn com.google.ai.edge.litertlm.**

# Google Mobile Ads (AdMob) and User Messaging Platform
-keep class com.google.android.gms.ads.** { *; }
-keep class com.google.android.ump.** { *; }
-dontwarn com.google.android.gms.ads.**

# AdMob mediation: Meta Audience Network
-keep class com.facebook.ads.** { *; }
-keep class com.google.ads.mediation.facebook.** { *; }
-dontwarn com.facebook.**
-dontwarn com.facebook.infer.annotation.Nullsafe$Mode
-dontwarn com.facebook.infer.annotation.Nullsafe

# AdMob mediation: AppLovin
-keep class com.applovin.** { *; }
-keep class com.google.ads.mediation.applovin.** { *; }
-dontwarn com.applovin.**

# Firebase (Analytics, Crashlytics, Performance)
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.measurement.** { *; }
-dontwarn com.google.firebase.**

# Room (entity reflection)
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Database class * { *; }
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# OkHttp / Okio (used by ModelDownloadWorker)
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

# Kotlin coroutines
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# Crashlytics: keep our app classes' source file + line numbers for stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
