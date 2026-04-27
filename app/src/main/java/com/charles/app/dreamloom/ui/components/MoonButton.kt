package com.charles.app.dreamloom.ui.components

import android.view.HapticFeedbackConstants
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.charles.app.dreamloom.ui.theme.DreamColors

/**
 * Pulsing moon CTA from [design/SYSTEM.md](file://../design/SYSTEM.md).
 */
@Composable
fun MoonButton(
    onClick: () -> Unit,
    size: Dp,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val view = LocalView.current
    val reduce = rememberPrefersReducedMotion()
    val transition = rememberInfiniteTransition(label = "moonPulse")
    val pulseAnim by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    val pulse = if (reduce) 1f else pulseAnim
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = tween(120),
        label = "press",
    )
    val combined = pulse * pressScale

    Box(
        modifier = modifier
            .size(size)
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            }
            .graphicsLayer {
                scaleX = combined
                scaleY = combined
            }
            .drawBehind {
                val r = this.size.minDimension / 2
                drawCircle(
                    color = DreamColors.Moonglow.copy(alpha = 0.2f),
                    radius = r * 1.32f,
                )
            }
            .clip(CircleShape)
            .background(
                brush = Brush.radialGradient(
                    0f to DreamColors.Moonglow.copy(alpha = 0.9f),
                    0.6f to DreamColors.AuroraSoft.copy(alpha = 0.3f),
                    1f to Color.Transparent,
                ),
            )
            .border(1.dp, DreamColors.Moonglow.copy(alpha = 0.25f), CircleShape)
            .clickable(
                interactionSource = interaction,
                indication = ripple(
                    bounded = true,
                    radius = size / 2,
                ),
            ) {
                view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.fillMaxSize(0.5f)) {
            val r = this.size.minDimension / 2
            val c = center
            drawCircle(
                color = DreamColors.Moonglow.copy(alpha = 0.95f),
                radius = r * 0.92f,
                center = c,
            )
            drawCircle(
                color = DreamColors.Night.copy(alpha = 0.88f),
                radius = r * 0.88f,
                center = Offset(c.x + r * 0.5f, c.y),
            )
        }
    }
}
