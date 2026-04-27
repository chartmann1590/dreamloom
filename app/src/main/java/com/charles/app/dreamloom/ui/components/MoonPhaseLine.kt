package com.charles.app.dreamloom.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import com.charles.app.dreamloom.core.lunar.MoonPhaseInfo
import com.charles.app.dreamloom.ui.theme.DreamColors
import androidx.compose.ui.unit.dp

/**
 * Tiny horizontal “phase” strip: lit length follows [MoonPhaseInfo.illumination], waxing from the left / waning from the right.
 */
@Composable
fun MoonPhaseLine(
    phase: MoonPhaseInfo,
    modifier: Modifier = Modifier,
) {
    val waning = phase.waning
    val lit = phase.illumination.coerceIn(0.02f, 1f)
    Canvas(
        modifier
            .width(88.dp)
            .height(2.dp),
    ) {
        val r = size.height / 2
        // Track
        drawRoundRect(
            color = DreamColors.InkFaint.copy(alpha = 0.28f),
            cornerRadius = CornerRadius(r, r),
            size = size,
        )
        val fillW = size.width * lit
        val topLeft = if (waning) {
            Offset(size.width - fillW, 0f)
        } else {
            Offset(0f, 0f)
        }
        drawRoundRect(
            brush = Brush.linearGradient(
                colorStops = arrayOf(
                    0f to DreamColors.Moonglow.copy(alpha = 0.5f),
                    1f to DreamColors.Moonglow,
                ),
                start = Offset(topLeft.x, size.height / 2),
                end = Offset(topLeft.x + fillW, size.height / 2),
            ),
            topLeft = topLeft,
            size = Size(fillW, size.height),
            cornerRadius = CornerRadius(r, r),
        )
    }
}
