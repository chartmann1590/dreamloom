package com.charles.app.dreamloom.ads

import android.app.Activity
import android.content.Context
import com.charles.app.dreamloom.BuildConfig
import com.charles.app.dreamloom.R
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterstitialManager @Inject constructor(
    @ApplicationContext private val app: Context,
) {
    private val adRef = AtomicReference<InterstitialAd?>(null)

    private val adUnitId: String
        get() = if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/1033173712"
        } else {
            app.getString(R.string.ad_int_post_detail)
        }

    fun preload() {
        InterstitialAd.load(
            app,
            adUnitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    adRef.set(null)
                }

                override fun onAdLoaded(ad: InterstitialAd) {
                    adRef.set(ad)
                }
            },
        )
    }

    /**
     * Shows the ad if one is ready. [onFinished] is invoked when the ad is dismissed, fails to show,
     * or when there was nothing to show. [onFinished] receives true if an ad was displayed.
     */
    fun showIfReady(activity: Activity, onFinished: (shown: Boolean) -> Unit) {
        val ad = adRef.getAndSet(null)
        if (ad == null) {
            // Fall back to a load-then-show path so fast back-nav still has a chance to show.
            InterstitialAd.load(
                app,
                adUnitId,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        onFinished(false)
                    }

                    override fun onAdLoaded(loadedAd: InterstitialAd) {
                        showLoaded(activity, loadedAd, onFinished)
                    }
                },
            )
            return
        }
        showLoaded(activity, ad, onFinished)
    }

    private fun showLoaded(
        activity: Activity,
        ad: InterstitialAd,
        onFinished: (shown: Boolean) -> Unit,
    ) {
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                onFinished(true)
                preload()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                onFinished(false)
                preload()
            }
        }
        ad.show(activity)
    }
}
