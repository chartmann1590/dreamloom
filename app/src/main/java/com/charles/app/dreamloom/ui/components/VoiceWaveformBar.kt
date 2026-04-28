package com.charles.app.dreamloom.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import com.charles.app.dreamloom.ui.theme.DreamColors
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun VoiceWaveformBar(
    isActive: Boolean,
    modifier: Modifier = Modifier,
) {
    val t = rememberInfiniteTransition(label = "wave")
    val phase by t.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "phase",
    )
    val fill = if (isActive) {
        Brush.horizontalGradient(
            0f to DreamColors.Moonglow.copy(alpha = 0.9f),
            0.5f to DreamColors.AuroraSoft.copy(alpha = 0.5f),
            1f to DreamColors.Moonglow.copy(alpha = 0.4f),
        )
    } else {
        Brush.horizontalGradient(
            0f to DreamColors.InkFaint.copy(alpha = 0.2f),
            1f to DreamColors.InkFaint.copy(alpha = 0.2f),
        )
    }
    Canvas(
        modifier
            .fillMaxWidth()
            .height(40.dp),
    ) {
        if (!isActive) {
            val w = 4.dp.toPx()
            val gap = 3.dp.toPx()
            var x = 0f
            while (x < size.width) {
                val h = size.height * 0.15f
                drawRoundRect(
                    brush = fill,
                    topLeft = Offset(x, (size.height - h) / 2f),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(2f, 2f),
                )
                x += w + gap
            }
            return@Canvas
        }
        val bars = 40
        val w = (size.width / (bars * 1.2f)).coerceAtMost(5.dp.toPx())
        val gap = w * 0.2f
        for (i in 0 until bars) {
            val x = i * (w + gap)
            val norm = (i + phase * bars) / bars
            val amp = 0.25f + 0.75f * (0.5f + 0.5f * sin(norm * 2f * Math.PI.toFloat() * 1.3f + phase * 6.28f)).let { it * it }
            val h = size.height * amp.coerceIn(0.2f, 1f)
            drawRoundRect(
                brush = fill,
                topLeft = Offset(x, (size.height - h) / 2f),
                size = Size(w, h),
                cornerRadius = CornerRadius(2f, 2f),
            )
        }
    }
}
