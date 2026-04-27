package com.charles.app.dreamloom.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.charles.app.dreamloom.data.prefs.ThemeMode

@Composable
fun DreamloomTheme(
    themeMode: ThemeMode = ThemeMode.Dark,
    content: @Composable () -> Unit,
) {
    val dark = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Dark -> true
        ThemeMode.Light -> false
    }
    val scheme = if (dark) {
        darkColorSchemeDream()
    } else {
        lightColorSchemeDream()
    }
    MaterialTheme(
        colorScheme = scheme,
        typography = DreamloomTypography,
        shapes = DreamShapes,
        content = content,
    )
}
