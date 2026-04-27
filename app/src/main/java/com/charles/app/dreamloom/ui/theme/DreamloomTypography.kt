package com.charles.app.dreamloom.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.charles.app.dreamloom.R

@OptIn(ExperimentalTextApi::class)
private val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

@OptIn(ExperimentalTextApi::class)
private val cormorantName = GoogleFont("Cormorant Garamond")
@OptIn(ExperimentalTextApi::class)
private val interName = GoogleFont("Inter")

@OptIn(ExperimentalTextApi::class)
val CormorantFont = FontFamily(Font(googleFont = cormorantName, fontProvider = provider, weight = FontWeight.Medium))
@OptIn(ExperimentalTextApi::class)
val CormorantFontItalic = FontFamily(
    Font(googleFont = cormorantName, fontProvider = provider, style = FontStyle.Italic),
)
@OptIn(ExperimentalTextApi::class)
val InterFont = FontFamily(Font(googleFont = interName, fontProvider = provider, weight = FontWeight.Normal))
@OptIn(ExperimentalTextApi::class)
val InterFontMedium = FontFamily(Font(googleFont = interName, fontProvider = provider, weight = FontWeight.Medium))
@OptIn(ExperimentalTextApi::class)
val InterFontSemiBold = FontFamily(Font(googleFont = interName, fontProvider = provider, weight = FontWeight.SemiBold))

@OptIn(ExperimentalTextApi::class)
val DreamloomTypography = androidx.compose.material3.Typography(
    displayLarge = TextStyle(
        fontFamily = CormorantFont,
        fontSize = 56.sp,
        lineHeight = 64.sp,
        fontWeight = FontWeight.Medium,
    ),
    displayMedium = TextStyle(fontFamily = CormorantFont, fontSize = 44.sp, lineHeight = 52.sp),
    displaySmall = TextStyle(fontFamily = CormorantFont, fontSize = 32.sp, lineHeight = 40.sp),
    headlineLarge = TextStyle(fontFamily = CormorantFont, fontSize = 26.sp, lineHeight = 34.sp),
    headlineMedium = TextStyle(fontFamily = CormorantFont, fontSize = 22.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = InterFontMedium, fontSize = 18.sp, fontWeight = FontWeight.Medium),
    titleMedium = TextStyle(fontFamily = InterFontMedium, fontSize = 16.sp, fontWeight = FontWeight.Medium),
    bodyLarge = TextStyle(fontFamily = InterFont, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = InterFont, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = InterFont, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(
        fontFamily = InterFontSemiBold,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = InterFont,
        fontSize = 11.sp,
        letterSpacing = 1.sp,
    ),
)
