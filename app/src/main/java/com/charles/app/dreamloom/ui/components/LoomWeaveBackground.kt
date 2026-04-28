package com.charles.app.dreamloom.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.charles.app.dreamloom.ui.theme.DreamColors
import androidx.compose.ui.util.lerp
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.hypot

/**
 * Gold threads slowly weaving across indigo — [spec/SCREENS.md](file://../../../spec/SCREENS.md) interpreting screen.
 */
@Composable
fun LoomWeaveBackground(
    modifier: Modifier = Modifier,
    lineColor: Color = DreamColors.Moonglow.copy(alpha = 0.28f),
    underlayColor: Color = DreamColors.IndigoSoft.copy(alpha = 0.5f),
) {
    val transition = rememberInfiniteTransition(label = "loom")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "phase",
    )
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(18_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "drift",
    )
    Canvas(modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val g = 8
        for (i in 0..g) {
            val t = (i + phase * 2f) / (g + 2f)
            val y = h * t
            val wobble = lerp(0.92f, 1.08f, kotlin.math.sin((i + phase * 6) * 0.7f) * 0.5f + 0.5f)
            drawLine(
                color = lineColor,
                start = Offset(0f, y * wobble),
                end = Offset(w, y * lerp(0.9f, 1.1f, drift) + 8f * kotlin.math.sin(phase * 2f * Math.PI.toFloat() + i)),
                strokeWidth = 1.2f,
                cap = StrokeCap.Round,
            )
        }
        val dPhase = phase * 2f * Math.PI.toFloat()
        for (c in 0..3) {
            val path = Path().apply {
                moveTo(0f, h * 0.2f * c + h * 0.1f * drift)
                for (s in 1..24) {
                    val x = w * s / 24f
                    val yy = h * 0.35f + 0.25f * h * kotlin.math.sin(s / 4f + dPhase + c) + x * 0.08f
                    lineTo(x, yy)
                }
            }
            drawPath(
                path,
                color = underlayColor,
                style = Stroke(
                    width = 1.4f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )
        }
        val cx = w * 0.5f + 40f * kotlin.math.sin(dPhase * 0.4f)
        val cy = h * 0.4f
        for (a in 0..5) {
            val ang = (a / 6f) * (2 * Math.PI).toFloat() + dPhase
            val len = hypot(w.toDouble(), h.toDouble()).toFloat() * 0.6f
            val ex = cx + len * kotlin.math.cos(ang)
            val ey = cy + len * kotlin.math.sin(ang)
            drawLine(
                color = lineColor.copy(alpha = 0.18f),
                start = Offset(cx, cy),
                end = Offset(ex, ey),
                strokeWidth = 1.1f,
            )
        }
    }
}
