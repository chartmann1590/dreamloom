package com.charles.app.dreamloom.llm.prompts

object InterpretPrompts {
    val SYSTEM: String = """
You are Dreamloom, a quiet, kind dream-companion. You help people interpret their dreams using a blend of Jungian, Freudian, and folkloric symbolism — without ever sounding like a textbook. You write in plain, lyrical prose. You never claim certainty about meaning. You never diagnose. You never mention being an AI, language model, or program.

When the user shares a dream, you respond ONLY in this exact format, with no preamble and no closing remarks:

TITLE: <a short evocative title, 3–7 words>
SYMBOLS: <comma-separated list of 3–6 single-word symbols, lowercase, no punctuation>
INTERPRETATION: <one paragraph, 100–140 words, lyrical, grounded, second person>
INTENTION: <one sentence, action-oriented, written for today>
    """.trimIndent()

    fun user(dreamText: String, mood: String) = """
Here is the dream:

"$dreamText"

The dreamer's mood was: $mood.

Interpret it now.
    """.trimIndent()

    fun userWithImage(dreamText: String, mood: String) = """
Here is the dream:

"$dreamText"

The dreamer's mood was: $mood.

The dreamer also drew this image. Use it as additional symbolic input — note any objects, colors, or forms you see.

Interpret it now.
    """.trimIndent()

    fun reInterpret(dreamText: String, previousSymbols: String) = """
Here is the dream:

"$dreamText"

Your previous interpretation focused on: $previousSymbols.

Now interpret it again from a different angle. Find different symbols. See what was hidden the first time.
    """.trimIndent()
}

fun gemmaChat(system: String, user: String) = buildString {
    append("<start_of_turn>user\n")
    append(system.trim())
    append("\n\n")
    append(user.trim())
    append("<end_of_turn>\n")
    append("<start_of_turn>model\n")
}
