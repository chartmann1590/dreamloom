package com.charles.app.dreamloom.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

fun darkColorSchemeDream(): ColorScheme = androidx.compose.material3.darkColorScheme(
    primary = DreamColors.Aurora,
    onPrimary = DreamColors.Ink,
    primaryContainer = DreamColors.IndigoSoft,
    secondary = DreamColors.Moonglow,
    onSecondary = DreamColors.Night,
    background = DreamColors.Night,
    surface = DreamColors.Indigo,
    onSurface = DreamColors.Ink,
    onBackground = DreamColors.Ink,
    error = DreamColors.Danger,
    onError = DreamColors.Ink,
    outline = DreamColors.InkFaint,
)

fun lightColorSchemeDream(): ColorScheme = androidx.compose.material3.lightColorScheme(
    primary = DreamColors.Aurora,
    onPrimary = Color(0xFFFFFFFF),
    background = Color(0xFFFAF7FF),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1A1240),
    onBackground = Color(0xFF1A1240),
)
