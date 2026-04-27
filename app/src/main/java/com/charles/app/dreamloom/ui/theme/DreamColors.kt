package com.charles.app.dreamloom.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object DreamColors {
    val Night = Color(0xFF0A0820)
    val Indigo = Color(0xFF1A1240)
    val IndigoSoft = Color(0xFF231854)
    val Violet = Color(0xFF2D1B69)
    val Aurora = Color(0xFF6B3FC4)
    val AuroraSoft = Color(0xFF8B5CF6)
    val Moonglow = Color(0xFFD4B483)
    val MoonglowSoft = Color(0xFFFBE9C3)
    val MoodSerene = Color(0xFF7DD3FC)
    val MoodAnxious = Color(0xFFE5B8FC)
    val MoodJoyful = Color(0xFFFCD34D)
    val MoodLost = Color(0xFF6B7280)
    val MoodFierce = Color(0xFFEF4444)
    val Ink = Color(0xFFF5F3FF)
    val InkMuted = Color(0xFFA5A4C7)
    val InkFaint = Color(0xFF6B6890)
    val Danger = Color(0xFFEF4444)
    val Success = Color(0xFF34D399)

    val backgroundGradient: Brush
        get() = Brush.verticalGradient(
            0f to Color(0xFF0F0A2E),
            0.5f to Violet,
            1f to Aurora,
        )
}
