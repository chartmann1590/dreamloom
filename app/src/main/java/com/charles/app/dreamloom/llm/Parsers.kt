package com.charles.app.dreamloom.llm

data class Interpretation(
    val title: String,
    val symbols: List<String>,
    val interpretation: String,
    val intention: String,
)

data class WeeklyInsight(
    val pattern: String,
    val summary: String,
    val invitation: String,
)

fun parseWeeklyInsight(raw: String): WeeklyInsight? {
    val pattern = Regex("""(?im)^\s*PATTERN\s*:\s*(.+)$""")
        .find(raw)?.groupValues?.get(1)?.trim()?.ifBlank { null } ?: return null
    val summary = Regex("""(?is)^\s*SUMMARY\s*:\s*([\s\S]*?)(?=^\s*INVITATION\s*:|\z)""", setOf(RegexOption.MULTILINE))
        .find(raw)?.groupValues?.get(1)?.trim()?.ifBlank { null } ?: return null
    val invitation = Regex("""(?is)^\s*INVITATION\s*:\s*([\s\S]+)$""", setOf(RegexOption.MULTILINE))
        .find(raw)?.groupValues?.get(1)?.trim()?.ifBlank { null } ?: return null
    return WeeklyInsight(pattern, summary, invitation)
}

fun parseInterpretation(raw: String): Interpretation? {
    val title = Regex("""(?im)^\s*TITLE\s*:\s*(.+)$""")
        .find(raw)?.groupValues?.get(1)?.trim()?.ifBlank { null } ?: return null

    val symbolsText = Regex("""(?is)^\s*SYMBOLS\s*:\s*([\s\S]*?)(?=^\s*INTERPRETATION\s*:|\z)""", setOf(RegexOption.MULTILINE))
        .find(raw)?.groupValues?.get(1)?.trim().orEmpty()
    val symbols = symbolsText
        .split(',', '\n', ';', '/')
        .map { it.trim().lowercase().trim('.', '-', '*', '•', ' ') }
        .filter { it.isNotEmpty() }
        .distinct()
    if (symbols.isEmpty()) return null

    val interpretation = Regex("""(?is)^\s*INTERPRETATION\s*:\s*([\s\S]*?)(?=^\s*INTENTION\s*:|\z)""", setOf(RegexOption.MULTILINE))
        .find(raw)?.groupValues?.get(1)?.trim()?.ifBlank { null } ?: return null

    val intention = Regex("""(?im)^\s*INTENTION\s*:\s*(.+)$""")
        .find(raw)?.groupValues?.get(1)?.trim()?.ifBlank { null } ?: return null

    return Interpretation(title, symbols, interpretation, intention)
}

/** Fields filled as the model output buffer grows; drives the Interpreting "loom" UI. */
data class StreamedInterpretation(
    val title: String? = null,
    val symbols: List<String> = emptyList(),
    val interpretation: String? = null,
    val intention: String? = null,
)

fun parseInterpretationStreaming(buffer: String): StreamedInterpretation {
    if (buffer.isBlank()) return StreamedInterpretation()
    val title = Regex("""(?i)TITLE:\s*([\s\S]*?)(?=\n\s*SYMBOLS:|\Z)""").find(buffer)
        ?.groupValues?.get(1)?.trim()?.ifBlank { null }
    val symbolsText = Regex("""(?i)SYMBOLS:\s*([\s\S]*?)(?=\n\s*INTERPRETATION:|\Z)""").find(buffer)
        ?.groupValues?.get(1)?.trim().orEmpty()
    val symbols = if (symbolsText.isNotEmpty()) {
        symbolsText.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
    } else {
        emptyList()
    }
    val interpretation = Regex("""(?i)INTERPRETATION:\s*([\s\S]*?)(?=\n\s*INTENTION:|\Z)""").find(buffer)
        ?.groupValues?.get(1)?.trim()?.ifBlank { null }
    val intention = Regex("""(?i)INTENTION:\s*([\s\S]+)""").find(buffer)
        ?.groupValues?.get(1)?.trim()?.ifBlank { null }
    return StreamedInterpretation(
        title = title,
        symbols = symbols,
        interpretation = interpretation,
        intention = intention,
    )
}
