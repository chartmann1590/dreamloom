package com.charles.app.dreamloom.ads

import android.app.Activity
import android.content.Context
import com.charles.app.dreamloom.BuildConfig
import com.charles.app.dreamloom.R
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

enum class RewardedPlacement {
    ExtendedInterpretation,
    WeeklyInsight,
}

@Singleton
class RewardedAdManager @Inject constructor(
    @ApplicationContext private val app: Context,
) {
    private val adMap: MutableMap<RewardedPlacement, RewardedAd?> = mutableMapOf(
        RewardedPlacement.ExtendedInterpretation to null,
        RewardedPlacement.WeeklyInsight to null,
    )

    private fun adUnitId(placement: RewardedPlacement): String {
        if (BuildConfig.DEBUG) {
            return "ca-app-pub-3940256099942544/5224354917"
        }
        return when (placement) {
            RewardedPlacement.ExtendedInterpretation -> app.getString(R.string.ad_rewarded)
            RewardedPlacement.WeeklyInsight -> app.getString(R.string.ad_rewarded)
        }
    }

    fun preload(placement: RewardedPlacement) {
        if (adMap[placement] != null) return
        RewardedAd.load(
            app,
            adUnitId(placement),
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    adMap[placement] = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    adMap[placement] = null
                }
            },
        )
    }

    fun show(
        activity: Activity,
        placement: RewardedPlacement,
        onFinished: (earnedReward: Boolean) -> Unit,
    ) {
        val cached = adMap[placement]
        if (cached == null) {
            RewardedAd.load(
                app,
                adUnitId(placement),
                AdRequest.Builder().build(),
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        adMap[placement] = ad
                        showLoaded(activity, placement, ad, onFinished)
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        onFinished(false)
                    }
                },
            )
            return
        }
        showLoaded(activity, placement, cached, onFinished)
    }

    private fun showLoaded(
        activity: Activity,
        placement: RewardedPlacement,
        ad: RewardedAd,
        onFinished: (earnedReward: Boolean) -> Unit,
    ) {
        var rewarded = false
        ad.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                adMap[placement] = null
                preload(placement)
                onFinished(rewarded)
            }

            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                adMap[placement] = null
                preload(placement)
                onFinished(false)
            }
        }
        ad.show(activity) { _: RewardItem ->
            rewarded = true
        }
    }
}
