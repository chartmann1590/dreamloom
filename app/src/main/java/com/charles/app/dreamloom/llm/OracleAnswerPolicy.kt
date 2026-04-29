package com.charles.app.dreamloom.llm

import java.util.Locale

object OracleAnswerPolicy {
    fun clean(raw: String): String {
        val stripped = raw
            .replace(Regex("""(?im)^\s*(TITLE|SYMBOLS|INTERPRETATION|INTENTION)\s*:\s*"""), "")
            .replace(Regex("""(?im)^\s*(PATTERN|SUMMARY|INVITATION)\s*:\s*"""), "")
            .trim()
        if (stripped.isEmpty()) return ""
        val sentenceSplit = Regex("""(?<=[.!?])\s+""")
        val sentences = stripped
            .split(sentenceSplit)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        if (sentences.isEmpty()) return stripped
        return if (sentences.size <= 5) sentences.joinToString(" ") else sentences.take(5).joinToString(" ")
    }

    fun isAcceptable(answer: String): Boolean {
        if (answer.length < 50) return false
        val alphaCount = answer.count { it.isLetter() }
        if (alphaCount < 35) return false
        val distinctChars = answer.lowercase(Locale.ROOT).filter { it.isLetter() }.toSet().size
        if (distinctChars < 8) return false
        if (Regex("""[a-zA-Z]{10,}""").find(answer) != null && !answer.contains(" ")) return false
        val sentenceCount = answer.split(Regex("""(?<=[.!?])\s+""")).count { it.isNotBlank() }
        return sentenceCount in 2..6
    }

    fun fallback(question: String, topSymbols: List<Pair<String, Int>>): String {
        val symbols = topSymbols.take(3).map { it.first }
        val symbolLine = if (symbols.isEmpty()) {
            "I answer from your question alone tonight."
        } else {
            "I keep returning through ${symbols.joinToString(", ")}."
        }
        return buildString {
            append(symbolLine)
            append(" ")
            append("Your question points to a choice that needs one honest next step, not perfect certainty. ")
            append("Follow what brings steadier breath and clearer attention, even if it feels smaller than your fear. ")
            append("Carry this into today: choose one small action that proves your direction.")
        }
    }
}
