import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.google.android.gms:oss-licenses-plugin:0.13.0")
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
    id("org.jetbrains.kotlin.plugin.serialization")
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
    compileSdk = 37

    lint {
        // The Compose runtime / lifecycle lint checks shipped with these libraries are compiled against a
        // newer Kotlin Analysis API than this AGP's lint, so their detectors crash with
        // IncompatibleClassChangeError (e.g. "Found class KaFunctionCall, but interface was expected").
        // Disable that family of checks until AGP's lint catches up with these libraries, then re-enable.
        disable += setOf(
            "RememberInComposition",
            "FrequentlyChangingValue",
            "NullSafeMutableLiveData",
            "AutoboxingStateCreation",
            "AutoboxingStateValueProperty",
            "CoroutineCreationDuringComposition",
            "FlowOperatorInvokedInComposition",
            "StateFlowValueCalledInComposition",
            "MutableCollectionMutableState",
            "UnrememberedMutableState",
            "RememberReturnType",
            "OpaqueUnitKey",
            "ProduceStateDoesNotAssignValue",
        )
    }

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
            }
        }
    }

    defaultConfig {
        applicationId = "com.charles.app.dreamloom"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        // GitHub token no longer travels through this app — held server-side as a
        // Cloudflare Worker secret instead (cloudflare-worker/). Owner is kept as a
        // plain, non-secret constant since FeedbackComponents.kt uses it to badge
        // developer replies.
        buildConfigField("String", "GITHUB_REPO_OWNER", "\"chartmann1590\"")
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
    implementation("androidx.core:core-ktx:1.19.1")

    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-process:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")

    implementation("androidx.navigation:navigation-compose:2.8.4")

    implementation("com.google.dagger:hilt-android:2.60.1")
    ksp("com.google.dagger:hilt-compiler:2.60.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.4.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    implementation("androidx.room:room-runtime:2.8.4")
    implementation("androidx.room:room-ktx:2.8.4")
    ksp("androidx.room:room-compiler:2.8.4")
    implementation("net.zetetic:sqlcipher-android:4.19.1")
    implementation("androidx.sqlite:sqlite-ktx:2.6.2")

    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.hilt:hilt-work:1.4.0")
    ksp("androidx.hilt:hilt-compiler:1.4.0")

    implementation("com.google.ai.edge.litertlm:litertlm-android:0.10.2")

    implementation("com.google.android.gms:play-services-ads:25.5.0")
    implementation("com.google.android.play:review-ktx:2.0.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.9.0")
    implementation("com.google.android.ump:user-messaging-platform:3.0.0")
    implementation("com.google.ads.mediation:facebook:6.18.0.0")
    implementation("com.google.ads.mediation:applovin:13.0.1.0")

    implementation(platform("com.google.firebase:firebase-bom:33.6.0"))
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-crashlytics-ktx")
    implementation("com.google.firebase:firebase-perf-ktx")

    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.google.android.gms:play-services-oss-licenses:17.1.0")
    implementation("com.squareup.okhttp3:okhttp:5.5.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

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
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:5.5.0")
    androidTestImplementation("androidx.test:rules:1.6.1")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.3.0")
}

// Kotlin compiler options for every KotlinCompile task (app and KSP). The old android.kotlinOptions
// block is an error under Kotlin 2.4. litertlm-android may be built with a newer Kotlin than our
// toolchain, hence the metadata check skip.
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
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
    val releaseStoreFile = localProperties.getProperty("dreamloom.release.storeFile")?.trim()
    val releaseStorePassword = localProperties.getProperty("dreamloom.release.storePassword")?.trim()
    val releaseKeyAlias = localProperties.getProperty("dreamloom.release.keyAlias")?.trim()
    val releaseKeyPassword = localProperties.getProperty("dreamloom.release.keyPassword")?.trim()
    if (
        releaseStoreFile.isNullOrBlank() ||
        releaseStorePassword.isNullOrBlank() ||
        releaseKeyAlias.isNullOrBlank() ||
        releaseKeyPassword.isNullOrBlank()
    ) {
        throw org.gradle.api.GradleException(
            "Release builds require configured signing credentials in local.properties: " +
                "dreamloom.release.storeFile, dreamloom.release.storePassword, " +
                "dreamloom.release.keyAlias, dreamloom.release.keyPassword.",
        )
    }
    val sha = dreamloomModelSha256
    if (sha.isNullOrBlank() || sha.equals("REPLACE_AT_BUILD_TIME", ignoreCase = true)) {
        throw org.gradle.api.GradleException(
            "Release builds require dreamloom.modelSha256 in local.properties (or -Pdreamloom.modelSha256). " +
                "Compute: shasum -a 256 gemma-4-E2B-it-int4.litertlm",
        )
    }
}
