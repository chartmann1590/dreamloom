package com.charles.app.dreamloom.telemetry

import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.perf.FirebasePerformance

object Telemetry {
    fun apply(context: Context, analyticsEnabled: Boolean, crashlyticsEnabled: Boolean) {
        FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(analyticsEnabled)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(crashlyticsEnabled)
        FirebasePerformance.getInstance().isPerformanceCollectionEnabled = analyticsEnabled
    }
}
