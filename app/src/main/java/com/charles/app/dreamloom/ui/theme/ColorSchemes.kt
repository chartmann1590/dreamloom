package com.charles.app.dreamloom.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

fun darkColorSchemeDream(): ColorScheme = androidx.compose.material3.darkColorScheme(
    primary = DreamColors.AuroraBright,
    onPrimary = DreamColors.NightDeep,
    primaryContainer = DreamColors.IndigoSoft,
    onPrimaryContainer = DreamColors.Ink,
    secondary = DreamColors.MoonglowBright,
    onSecondary = DreamColors.NightDeep,
    secondaryContainer = DreamColors.Indigo,
    onSecondaryContainer = DreamColors.MoonglowSoft,
    tertiary = DreamColors.Nebula,
    onTertiary = DreamColors.NightDeep,
    background = DreamColors.NightDeep,
    surface = DreamColors.Indigo,
    surfaceVariant = DreamColors.IndigoSoft,
    onSurface = DreamColors.Ink,
    onSurfaceVariant = DreamColors.InkMuted,
    onBackground = DreamColors.Ink,
    error = DreamColors.Danger,
    onError = DreamColors.Ink,
    outline = DreamColors.InkFaint,
    outlineVariant = DreamColors.IndigoSoft,
)

fun lightColorSchemeDream(): ColorScheme = androidx.compose.material3.lightColorScheme(
    primary = DreamColors.Aurora,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFEDE3FF),
    onPrimaryContainer = DreamColors.Indigo,
    secondary = Color(0xFFB48439),
    onSecondary = Color(0xFFFFFFFF),
    background = Color(0xFFFBF8FF),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFF1ECFC),
    onSurface = Color(0xFF1A1450),
    onSurfaceVariant = Color(0xFF4A4670),
    onBackground = Color(0xFF1A1450),
    outline = Color(0xFFB6B4D6),
)
