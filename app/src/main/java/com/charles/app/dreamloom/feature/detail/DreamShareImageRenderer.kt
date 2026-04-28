package com.charles.app.dreamloom.feature.detail

import android.graphics.Bitmap
import android.view.View.MeasureSpec
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.drawToBitmap
import com.charles.app.dreamloom.data.db.DreamEntity
import com.charles.app.dreamloom.data.prefs.ThemeMode
import com.charles.app.dreamloom.ui.theme.DreamloomTheme
import androidx.activity.ComponentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.math.roundToInt

object DreamShareImageRenderer {
    /**
     * Renders [DreamShareCard] to a bitmap; must run on the main thread.
     */
    suspend fun render(activity: ComponentActivity, dream: DreamEntity): Bitmap {
        return withContext(Dispatchers.Main.immediate) {
            val density = activity.resources.displayMetrics.density
            val widthPx = (360f * density).roundToInt().coerceIn(400, 1200)
            val parent = activity.window.decorView as ViewGroup
            val cv = ComposeView(activity).apply {
                setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
                setContent {
                    DreamloomTheme(ThemeMode.Dark) {
                        DreamShareCard(dream = dream)
                    }
                }
            }
            parent.addView(
                cv,
                ViewGroup.LayoutParams(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT),
            )
            try {
                suspendCancellableCoroutine { cont ->
                    cv.post {
                        val wSpec = MeasureSpec.makeMeasureSpec(widthPx, MeasureSpec.EXACTLY)
                        val hSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                        cv.measure(wSpec, hSpec)
                        val w = cv.measuredWidth.coerceAtLeast(1)
                        val h = cv.measuredHeight.coerceAtLeast(1)
                        cv.layout(0, 0, w, h)
                        if (h == 0 || w == 0) {
                            cont.resume(
                                Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888),
                            )
                            return@post
                        }
                        val out = runCatching { cv.drawToBitmap() }.getOrElse {
                            Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                        }
                        cont.resume(out)
                    }
                }
            } finally {
                parent.removeView(cv)
            }
        }
    }
}
