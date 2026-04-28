package com.charles.app.dreamloom.telemetry

import android.content.Context
import com.charles.app.dreamloom.data.prefs.AppPreferences
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Anonymous usage events per [INSTRUCTIONS.md] at the repo root.
 * Respects [AppPreferences.analyticsOptIn] on every call.
 */
@Singleton
class DreamloomAnalytics @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: AppPreferences,
) {
    private val fa: FirebaseAnalytics
        get() = FirebaseAnalytics.getInstance(context)

    suspend fun logDreamSaved(hasPhoto: Boolean, hasAudio: Boolean) {
        if (!prefs.analyticsOptIn.first()) return
        fa.logEvent("dream_saved") {
            param("has_photo", if (hasPhoto) 1L else 0L)
            param("has_audio", if (hasAudio) 1L else 0L)
        }
    }

    suspend fun logInterpretationCompleted(hadPhoto: Boolean) {
        if (!prefs.analyticsOptIn.first()) return
        fa.logEvent("interpretation_completed") {
            param("had_photo", if (hadPhoto) 1L else 0L)
        }
    }

    suspend fun logInterpretationFailed(hadPhoto: Boolean) {
        if (!prefs.analyticsOptIn.first()) return
        fa.logEvent("interpretation_failed") {
            param("had_photo", if (hadPhoto) 1L else 0L)
        }
    }

    suspend fun logReinterpretCompleted(hadPhoto: Boolean) {
        if (!prefs.analyticsOptIn.first()) return
        fa.logEvent("reinterpret_completed") {
            param("had_photo", if (hadPhoto) 1L else 0L)
        }
    }

    suspend fun logReinterpretFailed(hadPhoto: Boolean) {
        if (!prefs.analyticsOptIn.first()) return
        fa.logEvent("reinterpret_failed") {
            param("had_photo", if (hadPhoto) 1L else 0L)
        }
    }

    suspend fun logDreamDeleted() {
        if (!prefs.analyticsOptIn.first()) return
        fa.logEvent("dream_deleted", null)
    }

    suspend fun logModelDownloadCompleted(modelVersion: String) {
        if (!prefs.analyticsOptIn.first()) return
        fa.logEvent("model_download_completed") {
            param("model_version", modelVersion)
        }
    }

    suspend fun logWeeklyInsightGenerated() {
        if (!prefs.analyticsOptIn.first()) return
        fa.logEvent("weekly_insight_generated", null)
    }

    suspend fun logOracleAnswered() {
        if (!prefs.analyticsOptIn.first()) return
        fa.logEvent("oracle_answered", null)
    }
}
