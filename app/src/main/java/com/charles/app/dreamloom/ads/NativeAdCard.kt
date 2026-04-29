package com.charles.app.dreamloom.ads

import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.charles.app.dreamloom.BuildConfig
import com.charles.app.dreamloom.R
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.VideoOptions
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView

@Composable
fun NativeAdCard(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    val adUnitId = remember {
        if (BuildConfig.DEBUG) {
            "ca-app-pub-3940256099942544/2247696110"
        } else {
            context.getString(R.string.ad_native_advanced)
        }
    }

    LaunchedEffect(Unit) {
        val loader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad ->
                nativeAd?.destroy()
                nativeAd = ad
            }
            .withNativeAdOptions(
                NativeAdOptions.Builder()
                    .setVideoOptions(VideoOptions.Builder().setStartMuted(true).build())
                    .build(),
            )
            .build()
        loader.loadAd(AdRequest.Builder().build())
    }

    DisposableEffect(Unit) {
        onDispose {
            nativeAd?.destroy()
            nativeAd = null
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val root = NativeAdView(ctx)
            val content = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                val pad = (16 * ctx.resources.displayMetrics.density).toInt()
                setPadding(pad, pad, pad, pad)
            }
            val headline = TextView(ctx).apply {
                textSize = 18f
            }
            val body = TextView(ctx).apply {
                textSize = 14f
            }
            val cta = Button(ctx)

            content.addView(headline)
            content.addView(body)
            content.addView(cta)
            root.addView(content)

            root.headlineView = headline
            root.bodyView = body
            root.callToActionView = cta
            root
        },
        update = { adView ->
            val ad = nativeAd ?: return@AndroidView
            (adView.headlineView as? TextView)?.text = ad.headline
            val bodyView = adView.bodyView as? TextView
            bodyView?.text = ad.body.orEmpty()
            bodyView?.visibility = if (ad.body.isNullOrBlank()) View.GONE else View.VISIBLE

            val ctaView = adView.callToActionView as? Button
            ctaView?.text = ad.callToAction.orEmpty()
            ctaView?.visibility = if (ad.callToAction.isNullOrBlank()) View.GONE else View.VISIBLE
            adView.setNativeAd(ad)
        },
    )
}
