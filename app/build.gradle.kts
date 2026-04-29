import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.android.gms:oss-licenses-plugin:0.10.6")
    }
}

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
}

val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val dreamloomModelSha256: String? =
    localProperties.getProperty("dreamloom.modelSha256")?.trim()?.takeIf { it.isNotEmpty() }
        ?: (project.findProperty("dreamloom.modelSha256") as String?)?.trim()?.takeIf { it.isNotEmpty() }

android {
    namespace = "com.charles.app.dreamloom"
    compileSdk = 35

    signingConfigs {
        create("release") {
            val releaseStoreFile = localProperties.getProperty("dreamloom.release.storeFile")?.trim()
            val releaseStorePassword = localProperties.getProperty("dreamloom.release.storePassword")?.trim()
            val releaseKeyAlias = localProperties.getProperty("dreamloom.release.keyAlias")?.trim()
            val releaseKeyPassword = localProperties.getProperty("dreamloom.release.keyPassword")?.trim()

            if (!releaseStoreFile.isNullOrBlank()) {
                storeFile = file(releaseStoreFile)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            } else {
                // Local fallback so assembleRelease is installable without extra setup.
                storeFile = file("${System.getProperty("user.home")}/.android/debug.keystore")
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
    }

    defaultConfig {
        applicationId = "com.charles.app.dreamloom"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            val sha = dreamloomModelSha256 ?: ""
            buildConfigField("String", "MODEL_SHA256", "\"$sha\"")
        }
        debug {
            applicationIdSuffix = ".debug"
            buildConfigField("String", "MODEL_SHA256", "\"REPLACE_AT_BUILD_TIME\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            // litertlm-android is built with newer Kotlin; our toolchain stays 2.1
            "-Xskip-metadata-version-check",
        )
    }
    buildFeatures { compose = true; buildConfig = true }
    bundle {
        language { enableSplit = true }
        density { enableSplit = true }
        abi { enableSplit = true }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-text-google-fonts")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.core:core-ktx:1.15.0")

    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-process:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    implementation("androidx.navigation:navigation-compose:2.8.4")

    implementation("com.google.dagger:hilt-android:2.56.2")
    ksp("com.google.dagger:hilt-compiler:2.56.2")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")

    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    implementation("com.google.ai.edge.litertlm:litertlm-android:0.10.2")

    implementation("com.google.android.gms:play-services-ads:23.5.0")
    implementation("com.google.android.ump:user-messaging-platform:3.0.0")
    implementation("com.google.ads.mediation:facebook:6.18.0.0")
    implementation("com.google.ads.mediation:applovin:13.0.1.0")

    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-perf-ktx")

    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.google.android.gms:play-services-oss-licenses:17.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("androidx.room:room-testing:2.8.4")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("org.robolectric:robolectric:4.14.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
}

// KSP runs as KotlinCompile; it does not use android.kotlinOptions, so litertlm
// (Kotlin 2.3 metadata) needs this on all compile tasks.
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xskip-metadata-version-check")
    }
}

apply(plugin = "com.google.android.gms.oss-licenses-plugin")

gradle.taskGraph.whenReady {
    val runsReleaseArtifact = allTasks.any { t ->
        val n = t.name
        (n.startsWith("assemble") || n.startsWith("bundle")) && n.endsWith("Release")
    }
    if (!runsReleaseArtifact) return@whenReady
    val sha = dreamloomModelSha256
    if (sha.isNullOrBlank() || sha.equals("REPLACE_AT_BUILD_TIME", ignoreCase = true)) {
        throw org.gradle.api.GradleException(
            "Release builds require dreamloom.modelSha256 in local.properties (or -Pdreamloom.modelSha256). " +
                "Compute: shasum -a 256 gemma-4-E2B-it-int4.litertlm",
        )
    }
}
