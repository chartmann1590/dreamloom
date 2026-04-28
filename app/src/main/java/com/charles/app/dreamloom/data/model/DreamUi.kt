package com.charles.app.dreamloom.data.model

import androidx.compose.ui.graphics.Color
import com.charles.app.dreamloom.data.db.DreamEntity
import com.charles.app.dreamloom.ui.theme.DreamColors
import org.json.JSONArray
import org.json.JSONException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

fun DreamEntity.symbolsList(): List<String> = try {
    if (symbolsJson == null) emptyList() else {
        val arr = JSONArray(symbolsJson)
        (0 until arr.length()).map { arr.getString(it).trim().lowercase() }
    }
} catch (_: JSONException) {
    emptyList()
}

fun DreamEntity.hasSymbol(symbol: String): Boolean = symbolsList().any {
    it.equals(symbol, ignoreCase = true)
}

fun String.toMoodDotColor(): Color = when (lowercase()) {
    "serene" -> DreamColors.MoodSerene
    "anxious" -> DreamColors.MoodAnxious
    "joyful" -> DreamColors.MoodJoyful
    "lost" -> DreamColors.MoodLost
    "fierce" -> DreamColors.MoodFierce
    "skip" -> DreamColors.InkFaint
    else -> DreamColors.InkFaint
}

fun DreamEntity.formatDateLine(locale: Locale = Locale.getDefault()): Pair<String, String> {
    val z = ZoneId.systemDefault()
    val instant = Instant.ofEpochMilli(createdAt)
    val d = instant.atZone(z).toLocalDate()
    val date = DateTimeFormatter.ofPattern("MMM d", locale).format(d)
    val dow = DateTimeFormatter.ofPattern("EEE", locale).format(d)
    return date to dow
}

fun startOfThisWeekMs(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    while (cal.get(Calendar.DAY_OF_WEEK) != cal.firstDayOfWeek) {
        cal.add(Calendar.DAY_OF_WEEK, -1)
    }
    return cal.timeInMillis
}

fun startOfThisMonthMs(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.timeInMillis
}

/**
 * For FTS `MATCH` — tokenize and prefix each token (FTS5) with AND, avoiding invalid syntax.
 */
fun buildFtsMatchQuery(userInput: String): String? {
    val parts = userInput
        .trim()
        .split(Regex("\\s+"))
        .mapNotNull { part ->
            val t = part.filter { it.isLetterOrDigit() }
            if (t.isEmpty()) null else t
        }
    if (parts.isEmpty()) return null
    return parts.joinToString(" AND ") { p ->
        when {
            p.contains('*') || p.isEmpty() -> p
            else -> "$p*"
        }
    }
}

/**
 * All symbols in [dreams] with occurrence counts, sorted by count then name.
 */
fun symbolIndexFromDreams(dreams: List<DreamEntity>): List<Pair<String, Int>> {
    val counts = mutableMapOf<String, Int>()
    for (d in dreams) {
        for (s in d.symbolsList()) {
            counts[s] = (counts[s] ?: 0) + 1
        }
    }
    return counts.entries
        .map { (k, v) -> k to v }
        .sortedWith(compareBy({ -it.second }, { it.first }))
}

/**
 * From [com.charles.app.dreamloom.data.db.InsightEntity.topSymbolsJson] — objects `{ "s", "c" }`.
 */
fun topSymbolCountsFromInsightJson(json: String): List<Pair<String, Int>> = try {
    val arr = JSONArray(json)
    (0 until arr.length()).map { i ->
        val o = arr.getJSONObject(i)
        o.getString("s") to o.getInt("c")
    }
} catch (_: JSONException) {
    emptyList()
}
