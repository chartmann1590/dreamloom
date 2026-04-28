package com.charles.app.dreamloom.llm.prompts

import com.charles.app.dreamloom.data.db.DreamEntity
import com.charles.app.dreamloom.data.model.formatDateLine
import com.charles.app.dreamloom.data.model.symbolsList

object InsightOraclePrompts {

    fun weeklyInsightGemmaChat(
        dreamsOldestFirst: List<DreamEntity>,
        topSymbols: List<Pair<String, Int>>,
    ): String {
        val body = buildWeeklyUserBody(dreamsOldestFirst, topSymbols)
        return gemmaChat(InterpretPrompts.SYSTEM, body)
    }

    private fun buildWeeklyUserBody(
        dreamsOldestFirst: List<DreamEntity>,
        topSymbols: List<Pair<String, Int>>,
    ): String {
        val lines = StringBuilder()
        for (d in dreamsOldestFirst) {
            val (date, _) = d.formatDateLine()
            val text = d.rawText.trim().let { t ->
                if (t.length <= 200) t else t.take(200) + "…"
            }
            val syms = d.symbolsList().joinToString(", ").ifEmpty { "—" }
            lines.append("- $date: \"$text\" (mood: ${d.mood}, symbols: $syms)\n")
        }
        val topLine = if (topSymbols.isEmpty()) {
            "(none this period)"
        } else {
            topSymbols.joinToString { "${it.first} (${it.second})" }
        }
        return """
            Here are dreams to reflect on, oldest first:

            ${lines.toString().trimEnd()}

            The recurring symbols in this set were: $topLine

            Write a weekly reflection in this exact format:

            PATTERN: <one sentence naming the dominant pattern of this stretch>
            SUMMARY: <60–80 words, second person, lyrical, names the symbols and the emotional arc>
            INVITATION: <one short sentence inviting a small action for the coming week>
        """.trimIndent()
    }

    fun oracleGemmaChat(
        topSymbols: List<Pair<String, Int>>,
        question: String,
    ): String {
        val list = if (topSymbols.isEmpty()) {
            "(no symbol history yet—answer gently from the question alone.)"
        } else {
            topSymbols.joinToString { "${it.first} ×${it.second}" }
        }
        val user = """
            The dreamer's recent recurring symbols (most frequent first, last 30 days):
            $list

            The dreamer asks: "$question"

            Answer in 3–5 sentences. Speak as the dream itself, not about it. Do not list the symbols — weave them in naturally where they fit. End with one sentence the dreamer can carry into their day.
        """.trimIndent()
        return gemmaChat(InterpretPrompts.SYSTEM, user)
    }
}
