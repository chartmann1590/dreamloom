package com.charles.app.dreamloom.llm.prompts

import com.charles.app.dreamloom.data.db.DreamEntity
import com.charles.app.dreamloom.data.model.formatDateLine
import com.charles.app.dreamloom.data.model.symbolsList

/**
 * Plain user-message bodies for Insight (weekly) and Oracle features.
 *
 * The system prompt ([InterpretPrompts.SYSTEM]) is passed separately to
 * [com.charles.app.dreamloom.llm.LlmEngine.generateStream]'s `systemPrompt`
 * parameter. Do not pre-wrap with chat-template tokens — LiteRT-LM applies
 * the model's correct template (Gemma 4 uses `<|turn>...<turn|>`) at
 * tokenization time.
 */
object InsightOraclePrompts {
    val WEEKLY_INSIGHT_SYSTEM: String = """
You are Dreamloom's weekly reflection voice.
You must output ONLY this exact 3-field format with uppercase labels:
PATTERN: <one sentence naming the dominant pattern in this week of dreams>
SUMMARY: <60-80 words in second person, lyrical and grounded, describing symbols and emotional arc>
INVITATION: <one short sentence inviting one small concrete action this week>
Do not output TITLE, SYMBOLS, INTERPRETATION, INTENTION, markdown, bullets, or extra labels.
    """.trimIndent()

    val ORACLE_SYSTEM: String = """
You are Dreamloom's Oracle voice.
Respond directly to the user's question in 3-5 sentences.
Use plain, vivid language. Be specific, warm, and grounded.
Speak as the dream's voice ("I"/"we"), not as an analyst.
Do not output labels, markdown, bullet points, or section headers.
Do not mention being an AI or model.
End with one short sentence the dreamer can carry into their day.
    """.trimIndent()

    val ORACLE_STRICT_SYSTEM: String = """
You are Dreamloom's Oracle voice.
Write exactly 4 sentences.
Each sentence must be plain English and directly relevant to the user's question.
No labels, no markdown, no lists, no symbols section, no roleplay instructions.
Do not output random letters or unfinished fragments.
End with one practical line the dreamer can carry into today.
    """.trimIndent()

    fun weeklyInsightUser(
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

    fun oracleUser(
        topSymbols: List<Pair<String, Int>>,
        question: String,
    ): String {
        val list = if (topSymbols.isEmpty()) {
            "(no symbol history yet—answer gently from the question alone.)"
        } else {
            topSymbols.joinToString { "${it.first} ×${it.second}" }
        }
        return """
            The dreamer's recent recurring symbols (most frequent first, last 30 days):
            $list

            The dreamer asks: "$question"

            Answer in 3-5 sentences. Speak as the dream itself, not about it.
            Do not list the symbols mechanically; weave them in naturally where they fit.
            Keep it concrete and relevant to this exact question.
        """.trimIndent()
    }
}
