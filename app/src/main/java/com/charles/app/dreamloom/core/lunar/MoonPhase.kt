package com.charles.app.dreamloom.core.lunar

import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.cos
import kotlin.math.roundToInt
/**
 * Local lunar phase for a calendar day (e.g. "last night" = yesterday).
 * Synodic month ~29.53d; no network.
 */
data class MoonPhaseInfo(
    val label: String,
    /** 1–29 in the current lunation for display, e.g. "day 4" */
    val dayInLunation: Int,
    /** 0f…1f — lit portion of the visible disk */
    val illumination: Float,
) {
    val waning: Boolean
        get() = label.startsWith("waning")
}

private const val SYNODIC = 29.530588

/** Approximate new moon start reference (UTC calendar); phase math is display-grade. */
private val newMoonRef: LocalDate = LocalDate.of(2000, 1, 6)

fun moonPhaseForLocalDate(date: LocalDate): MoonPhaseInfo {
    var days = ChronoUnit.DAYS.between(newMoonRef, date).toDouble() % SYNODIC
    if (days < 0) days += SYNODIC
    val age = (days / SYNODIC).toFloat().coerceIn(0f, 0.999999f)
    // Illumination: 0 at new, 1 at full, 0 at new
    val illumination = (0.5 * (1.0 - cos(2.0 * Math.PI * age))).toFloat()
    val day = (days + 0.5).roundToInt().coerceIn(1, 29)
    return MoonPhaseInfo(label = phaseName(age), dayInLunation = day, illumination = illumination)
}

/**
 * "Last night" in [zone] — previous local calendar day.
 */
fun lastNightMoonPhase(zoneId: ZoneId = ZoneId.systemDefault()): MoonPhaseInfo {
    val y = LocalDate.now(zoneId).minusDays(1)
    return moonPhaseForLocalDate(y)
}

/** Phase fraction 0 = new, 0.5 = full, 1 = new. */
private fun phaseName(age: Float): String = when {
    age >= 0.97f || age <= 0.04f -> "new"
    age < 0.20f -> "waxing crescent"
    age < 0.27f -> "first quarter"
    age < 0.46f -> "waxing gibbous"
    age < 0.54f -> "full"
    age < 0.73f -> "waning gibbous"
    age < 0.80f -> "last quarter"
    else -> "waning crescent"
}
