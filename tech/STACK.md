# Tech Stack

All versions are floor versions known to work as of April 2026. Cursor: bump to latest stable in each line if compatible — but verify Compose BOM compatibility before bumping core libraries.

---

## Project setup

- **Android Studio**: Ladybug | 2024.2.x or newer
- **AGP**: 8.7.0
- **Gradle**: 8.10
- **Kotlin**: 2.1.0
- **JDK**: 17

`gradle.properties`:
```
org.gradle.jvmargs=-Xmx4g -XX:MaxMetaspaceSize=1g
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

## SDKs

- **minSdk** = 28 (Android 9.0). LiteRT-LM with int4 needs ARMv8.2 for best performance, which lines up with API 28+.
- **targetSdk** = 35 (Android 15) at minimum. Bump to 36 once stable.
- **compileSdk** = matches targetSdk.

## App-level `build.gradle.kts` — dependencies

```kotlin
dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    // Activity / Lifecycle
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.4")

    // DI — Hilt
    implementation("com.google.dagger:hilt-android:2.52")
    ksp("com.google.dagger:hilt-compiler:2.52")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // Storage — Room with FTS + SQLCipher
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")

    // DataStore (preferences only — Room owns dream data)
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Encrypted prefs (for the SQLCipher passphrase)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // WorkManager (model download + reminder scheduling)
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // ===== ON-DEVICE LLM =====
    implementation("com.google.ai.edge.litert:litert-genai:1.0.0-beta01")
    // (LiteRT core is brought in transitively; pin if needed:)
    implementation("com.google.ai.edge.litert:litert:1.0.1")

    // ===== ADS =====
    implementation("com.google.android.gms:play-services-ads:23.5.0")
    // UMP for EU consent
    implementation("com.google.android.ump:user-messaging-platform:3.0.0")

    // Mediation adapters (configure in AdMob console too)
    implementation("com.google.ads.mediation:facebook:6.18.0.0")
    implementation("com.google.ads.mediation:applovin:13.0.1.0")

    // ===== FIREBASE =====
    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    // Image loading
    implementation("io.coil-kt:coil-compose:2.7.0")

    // OSS licenses screen
    implementation("com.google.android.gms:play-services-oss-licenses:17.1.0")

    // Test
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("io.mockk:mockk:1.13.13")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
}
```

## Plugins

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.android.gms.oss-licenses-plugin")
}
```

## ProGuard / R8 (release only)

```pro
# Hilt
-keep class dagger.hilt.** { *; }
# Room — auto-handled
# LiteRT-LM — keep generative APIs
-keep class com.google.ai.edge.litert.** { *; }
-keep class com.google.android.gms.ads.** { *; }
# Crashlytics needs unobfuscated stack traces — generate mapping
```

Enable `minifyEnabled true` in release. Keep mapping file uploaded to Crashlytics.

## App Bundle settings

```kotlin
android {
    bundle {
        language { enableSplit = true }
        density  { enableSplit = true }
        abi      { enableSplit = true }
    }
}
```

We do **not** bundle the model in the AAB (it would push us past Play's 200MB AAB limit and waste install bandwidth for users who never finish onboarding). See `tech/MODEL_DOWNLOAD.md`.

## Permissions (AndroidManifest)

```xml
<uses-permission android:name="android.permission.INTERNET" />  <!-- model download + ads only -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />  <!-- optional reminders -->
<uses-permission android:name="android.permission.AD_ID" />  <!-- AdMob -->

<!-- Hardware -->
<uses-feature android:name="android.hardware.microphone" android:required="false" />
```

Note: no `READ_EXTERNAL_STORAGE` — photo attachments use the photo picker (`ActivityResultContracts.PickVisualMedia`) which doesn't require permissions on API 33+.

## Module structure

Single Gradle module is fine for MVP — Compose and Hilt make over-modularization premature. If the app grows past ~30 KLOC, split into `:core`, `:data`, `:llm`, `:ads`, `:feature-dream`, etc. Don't split until then.
