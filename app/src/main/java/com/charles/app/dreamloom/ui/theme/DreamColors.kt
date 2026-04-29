package com.charles.app.dreamloom.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object DreamColors {
    val Night = Color(0xFF09071C)
    val NightDeep = Color(0xFF050315)
    val Indigo = Color(0xFF1A1450)
    val IndigoSoft = Color(0xFF2A1E6E)
    val Violet = Color(0xFF3A1F8C)
    val VioletBright = Color(0xFF5B2EE0)
    val Aurora = Color(0xFF7C3AED)
    val AuroraSoft = Color(0xFFA371FF)
    val AuroraBright = Color(0xFFC084FC)
    val Nebula = Color(0xFFFF6FD8)
    val Cyanwash = Color(0xFF38BDF8)
    val Moonglow = Color(0xFFE8C77A)
    val MoonglowBright = Color(0xFFFFD98A)
    val MoonglowSoft = Color(0xFFFCE9B8)

    val MoodSerene = Color(0xFF7DD3FC)
    val MoodAnxious = Color(0xFFE5B8FC)
    val MoodJoyful = Color(0xFFFFD24D)
    val MoodLost = Color(0xFF8B8AB0)
    val MoodFierce = Color(0xFFFB6E6E)

    val Ink = Color(0xFFF5F3FF)
    val InkMuted = Color(0xFFB6B4D6)
    val InkFaint = Color(0xFF7975A0)
    val Danger = Color(0xFFFB6E6E)
    val Success = Color(0xFF34D399)

    val backgroundGradient: Brush
        get() = Brush.verticalGradient(
            0f to NightDeep,
            0.35f to Indigo,
            0.7f to Violet,
            1f to Aurora,
        )

    val auroraSweep: Brush
        get() = Brush.linearGradient(
            0f to Aurora,
            0.5f to Nebula,
            1f to Cyanwash,
        )

    val moonGradient: Brush
        get() = Brush.radialGradient(
            0f to MoonglowSoft,
            0.6f to MoonglowBright,
            1f to Moonglow,
        )
}
