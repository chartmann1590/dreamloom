package com.charles.app.dreamloom.data.model

/**
 * Must match what [Recording] / mood entry uses.
 */
val dreamMoodOptionKeys: List<String> = listOf(
    "serene", "anxious", "joyful", "lost", "fierce", "skip",
)

fun moodLabelForKey(key: String): String = when (key) {
    "serene" -> "Serene"
    "anxious" -> "Anxious"
    "joyful" -> "Joyful"
    "lost" -> "Lost"
    "fierce" -> "Fierce"
    "skip" -> "—"
    else -> key.replaceFirstChar { c -> c.uppercase() }
}
