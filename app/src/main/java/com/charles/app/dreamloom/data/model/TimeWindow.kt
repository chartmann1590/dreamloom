package com.charles.app.dreamloom.data.model

/** Start of the calendar week, same definition as [startOfThisWeekMs] (device locale), as epoch day. */
fun weekStartEpochDayForNow(): Long {
    val startMs = startOfThisWeekMs()
    val z = java.time.ZoneId.systemDefault()
    return java.time.Instant.ofEpochMilli(startMs)
        .atZone(z)
        .toLocalDate()
        .toEpochDay()
}

/** Cutoff for “last 30 days” (Oracle symbol window). */
fun startOfLast30DaysMs(): Long = System.currentTimeMillis() - 30L * 24L * 60L * 60L * 1000L
