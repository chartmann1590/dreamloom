package com.charles.app.dreamloom.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "dreamloom")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val ds = context.dataStore

    val onboardingComplete: Flow<Boolean> = ds.data.map { it[KEY_ONBOARDING] == true }
    val themeMode: Flow<ThemeMode> = ds.data.map {
        when (it[KEY_THEME] ?: 0) {
            1 -> ThemeMode.Light
            2 -> ThemeMode.System
            else -> ThemeMode.Dark
        }
    }
    val analyticsOptIn: Flow<Boolean> = ds.data.map { it[KEY_ANALYTICS] != false }
    val crashlyticsOptIn: Flow<Boolean> = ds.data.map { it[KEY_CRASH] != false }
    val lastAppOpenAdAt: Flow<Long> = ds.data.map { it[KEY_APP_OPEN] ?: 0L }
    val interstitialThisSession: Flow<Boolean> = ds.data.map { it[KEY_INT_SESSION] == true }
    val lastInterstitialAt: Flow<Long> = ds.data.map { it[KEY_INT_AT] ?: 0L }
    val sessionStartMs: Flow<Long> = ds.data.map { it[KEY_SESS_START] ?: 0L }
    val reminderEnabled: Flow<Boolean> = ds.data.map { it[KEY_REM] == true }
    val reminderHour: Flow<Int> = ds.data.map { it[KEY_REM_H] ?: 7 }
    val reminderMinute: Flow<Int> = ds.data.map { it[KEY_REM_M] ?: 30 }
    val reminderText: Flow<String> = ds.data.map {
        it[KEY_REM_T] ?: "Tap the moon. While the dream is still warm."
    }
    val autoStopAfterSilence: Flow<Boolean> = ds.data.map { it[KEY_AUTO_STOP] != false }
    val firstInstallSessionDone: Flow<Boolean> = ds.data.map { it[KEY_FIRST] == true }
    val userPromptedCellular: Flow<Boolean> = ds.data.map { it[KEY_CELLULAR] == true }
    val wifiOnlyDownload: Flow<Boolean> = ds.data.map { it[KEY_WIFI_ONLY] != false }
    val insightGated: Flow<Boolean> = ds.data.map { it[KEY_INSIGHT_GATE] == true }
    val extendedGated: Flow<Boolean> = ds.data.map { it[KEY_EXT_GATE] == true }

    suspend fun setOnboardingComplete(v: Boolean) = ds.edit { it[KEY_ONBOARDING] = v }
    suspend fun setTheme(mode: ThemeMode) = ds.edit {
        it[KEY_THEME] = when (mode) {
            ThemeMode.Dark -> 0
            ThemeMode.Light -> 1
            ThemeMode.System -> 2
        }
    }
    suspend fun setAnalytics(v: Boolean) = ds.edit { it[KEY_ANALYTICS] = v }
    suspend fun setCrashlytics(v: Boolean) = ds.edit { it[KEY_CRASH] = v }
    suspend fun markAppOpenAdShown() = ds.edit { it[KEY_APP_OPEN] = System.currentTimeMillis() }
    suspend fun setInterstitialShown() = ds.edit {
        it[KEY_INT_SESSION] = true
        it[KEY_INT_AT] = System.currentTimeMillis()
    }
    suspend fun resetInterstitialForSession() = ds.edit { it[KEY_INT_SESSION] = false }
    suspend fun setSessionStart() = ds.edit { it[KEY_SESS_START] = System.currentTimeMillis() }
    suspend fun setReminderEnabled(v: Boolean) = ds.edit { it[KEY_REM] = v }
    suspend fun setReminderTime(h: Int, m: Int) = ds.edit {
        it[KEY_REM_H] = h
        it[KEY_REM_M] = m
    }
    suspend fun setReminderText(s: String) = ds.edit { it[KEY_REM_T] = s }
    suspend fun setAutoStop(v: Boolean) = ds.edit { it[KEY_AUTO_STOP] = v }
    suspend fun setFirstInstallSessionDone() = ds.edit { it[KEY_FIRST] = true }
    suspend fun setUserPromptedCellular(v: Boolean) = ds.edit { it[KEY_CELLULAR] = v }
    suspend fun setWifiOnly(v: Boolean) = ds.edit { it[KEY_WIFI_ONLY] = v }
    suspend fun setInsightGated(v: Boolean) = ds.edit { it[KEY_INSIGHT_GATE] = v }
    suspend fun setExtendedGated(v: Boolean) = ds.edit { it[KEY_EXT_GATE] = v }
    suspend fun clearAll() = ds.edit { it.clear() }

    private companion object {
        val KEY_ONBOARDING = booleanPreferencesKey("onboarding_complete")
        val KEY_THEME = intPreferencesKey("theme_mode")
        val KEY_ANALYTICS = booleanPreferencesKey("analytics")
        val KEY_CRASH = booleanPreferencesKey("crashlytics")
        val KEY_APP_OPEN = longPreferencesKey("last_app_open_ad")
        val KEY_INT_SESSION = booleanPreferencesKey("int_session")
        val KEY_INT_AT = longPreferencesKey("last_int")
        val KEY_SESS_START = longPreferencesKey("session_start")
        val KEY_REM = booleanPreferencesKey("reminder_en")
        val KEY_REM_H = intPreferencesKey("reminder_h")
        val KEY_REM_M = intPreferencesKey("reminder_m")
        val KEY_REM_T = stringPreferencesKey("reminder_text")
        val KEY_AUTO_STOP = booleanPreferencesKey("auto_stop_silence")
        val KEY_FIRST = booleanPreferencesKey("first_session_done")
        val KEY_CELLULAR = booleanPreferencesKey("cellular_prompted")
        val KEY_WIFI_ONLY = booleanPreferencesKey("wifi_only")
        val KEY_INSIGHT_GATE = booleanPreferencesKey("insight_gated")
        val KEY_EXT_GATE = booleanPreferencesKey("extended_gated")
    }
}
