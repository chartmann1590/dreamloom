package com.charles.app.dreamloom.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.charles.app.dreamloom.ui.theme.DreamColors
import kotlin.random.Random

/**
 * Root canvas: vertical aurora gradient + faint star specks (≈8% layer) from [design/SYSTEM.md](file://../design/SYSTEM.md).
 */
@Composable
fun AuroraStarfieldBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val seed = remember { Random(0xBEE5L) }
    val stars = remember {
        List(64) {
            Triple(seed.nextFloat(), seed.nextFloat(), 0.4f + seed.nextFloat() * 0.6f)
        }
    }
    Box(
        modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0f to Color(0xFF0F0A2E),
                    0.5f to DreamColors.Violet,
                    1f to DreamColors.Aurora,
                ),
            ),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val n = size
            stars.forEach { (u, v, a) ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.08f * a),
                    center = Offset(u * n.width, v * n.height),
                    radius = 1.2f,
                )
            }
        }
        content()
    }
}
