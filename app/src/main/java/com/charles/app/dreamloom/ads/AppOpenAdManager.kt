package com.charles.app.dreamloom.ads

import android.app.Activity
import android.content.Context
import com.charles.app.dreamloom.BuildConfig
import com.charles.app.dreamloom.R
import com.charles.app.dreamloom.data.prefs.AppPreferences
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppOpenAdManager @Inject constructor(
    @ApplicationContext private val app: Context,
    private val adGate: AdGate,
    private val prefs: AppPreferences,
) {
    private var appOpenAd: AppOpenAd? = null
    private var isLoading: Boolean = false
    private var isShowing: Boolean = false

    private val adUnitId: String
        get() = if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/9257395921"
        } else {
            app.getString(R.string.ad_app_open_main)
        }

    fun preload() {
        if (isLoading || appOpenAd != null) return
        isLoading = true
        AppOpenAd.load(
            app,
            adUnitId,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    isLoading = false
                    appOpenAd = ad
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoading = false
                    appOpenAd = null
                }
            },
        )
    }

    suspend fun showIfEligible(
        activity: Activity,
        openedFromNotification: Boolean,
    ): Boolean {
        if (isShowing) return false
        if (openedFromNotification) return false
        val firstSessionDone = prefs.firstInstallSessionDone.first()
        val lastShownAt = prefs.lastAppOpenAdAt.first()
        if (!adGate.canShowAppOpen(firstSessionDone, lastShownAt)) {
            preload()
            return false
        }
        val ad = appOpenAd ?: run {
            preload()
            return false
        }

        isShowing = true
        ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                isShowing = false
                appOpenAd = null
                preload()
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                isShowing = false
                appOpenAd = null
                preload()
            }
        }
        ad.show(activity)
        prefs.markAppOpenAdShown()
        return true
    }
}
