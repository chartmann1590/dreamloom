package com.charles.app.dreamloom

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.charles.app.dreamloom.data.prefs.AppPreferences
import com.charles.app.dreamloom.telemetry.Telemetry
import com.charles.app.dreamloom.work.ReminderScheduler
import com.charles.app.dreamloom.work.WeeklyInsightWorkScheduler
import com.google.android.gms.ads.MobileAds
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltAndroidApp
class DreamloomApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var appPreferences: AppPreferences

    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        MobileAds.initialize(this) {}
        runBlocking {
            Telemetry.apply(
                this@DreamloomApplication,
                appPreferences.analyticsOptIn.first(),
                appPreferences.crashlyticsOptIn.first(),
            )
            appPreferences.setSessionStart()
            appPreferences.resetInterstitialForSession()
            reminderScheduler.syncFromPreferences(appPreferences)
        }
        WeeklyInsightWorkScheduler.schedule(this)
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            // LlmEngine kept warm per spec; do not release at BACKGROUND
        }
    }
}
