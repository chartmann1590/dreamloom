package com.charles.app.dreamloom.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.charles.app.dreamloom.ui.theme.DreamColors
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private data class Star(
    val u: Float,
    val v: Float,
    val baseAlpha: Float,
    val radius: Float,
    val phase: Float,
)

/**
 * Root canvas: layered aurora gradient + twinkling starfield + soft nebula glows.
 * Twinkling respects reduced-motion preferences.
 */
@Composable
fun AuroraStarfieldBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val seed = remember { Random(0xBEE5L) }
    val stars = remember {
        List(96) {
            Star(
                u = seed.nextFloat(),
                v = seed.nextFloat(),
                baseAlpha = 0.05f + seed.nextFloat() * 0.18f,
                radius = 0.6f + seed.nextFloat() * 1.6f,
                phase = seed.nextFloat() * (2f * PI.toFloat()),
            )
        }
    }
    val reduce = rememberPrefersReducedMotion()
    val transition = rememberInfiniteTransition(label = "starfield")
    val twinkleAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(8_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "twinkle",
    )
    val twinkle = if (reduce) 0f else twinkleAnim

    Box(
        modifier
            .fillMaxSize()
            .background(DreamColors.backgroundGradient)
            .drawBehind {
                // Soft nebula glow (lower-left, magenta) — single radial paint, cheap.
                drawCircle(
                    brush = Brush.radialGradient(
                        0f to DreamColors.Nebula.copy(alpha = 0.18f),
                        1f to Color.Transparent,
                        center = Offset(size.width * 0.15f, size.height * 0.85f),
                        radius = size.minDimension * 0.55f,
                    ),
                    radius = size.minDimension * 0.55f,
                    center = Offset(size.width * 0.15f, size.height * 0.85f),
                )
                // Cyan glow (upper-right) — anchors the dimensional feel.
                drawCircle(
                    brush = Brush.radialGradient(
                        0f to DreamColors.Cyanwash.copy(alpha = 0.10f),
                        1f to Color.Transparent,
                        center = Offset(size.width * 0.92f, size.height * 0.12f),
                        radius = size.minDimension * 0.45f,
                    ),
                    radius = size.minDimension * 0.45f,
                    center = Offset(size.width * 0.92f, size.height * 0.12f),
                )
            },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            stars.forEach { s ->
                val flicker = if (reduce) 1f else (0.55f + 0.45f * sin(twinkle + s.phase))
                drawCircle(
                    color = Color.White.copy(alpha = s.baseAlpha * flicker),
                    center = Offset(s.u * size.width, s.v * size.height),
                    radius = s.radius,
                )
            }
        }
        content()
    }
}
