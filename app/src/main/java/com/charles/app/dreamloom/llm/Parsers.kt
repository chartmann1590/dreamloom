package com.charles.app.dreamloom.llm

data class Interpretation(
    val title: String,
    val symbols: List<String>,
    val interpretation: String,
    val intention: String,
)

fun parseInterpretation(raw: String): Interpretation? {
    val title = Regex("TITLE:\\s*(.+)").find(raw)?.groupValues?.get(1)?.trim() ?: return null
    val symbols = Regex("SYMBOLS:\\s*(.+)").find(raw)?.groupValues?.get(1)
        ?.split(",")
        ?.map { it.trim().lowercase() }
        ?.filter { it.isNotEmpty() } ?: return null
    val interpretation = Regex("INTERPRETATION:\\s*([\\s\\S]+?)(?=\\nINTENTION:|$)")
        .find(raw)?.groupValues?.get(1)?.trim() ?: return null
    val intention = Regex("INTENTION:\\s*(.+)").find(raw)?.groupValues?.get(1)?.trim() ?: return null
    return Interpretation(title, symbols, interpretation, intention)
}
