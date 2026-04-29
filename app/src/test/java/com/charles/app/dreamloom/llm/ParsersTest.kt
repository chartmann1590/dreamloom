package com.charles.app.dreamloom.llm

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ParsersTest {
    @Test
    fun parseWeeklyInsight_parsesCanonicalOutput() {
        val raw = """
            PATTERN: You keep moving from danger toward belonging.
            SUMMARY: Through shifting streets, locked rooms, and the returning train, you keep choosing connection over control, and your fear softens each time you ask for help.
            INVITATION: Send one honest message you have been postponing.
        """.trimIndent()

        val parsed = parseWeeklyInsight(raw)

        requireNotNull(parsed)
        assertEquals("You keep moving from danger toward belonging.", parsed.pattern)
        assertEquals(
            "Through shifting streets, locked rooms, and the returning train, you keep choosing connection over control, and your fear softens each time you ask for help.",
            parsed.summary,
        )
        assertEquals("Send one honest message you have been postponing.", parsed.invitation)
    }

    @Test
    fun parseWeeklyInsight_parsesCaseAndWhitespaceVariants() {
        val raw = """
            pattern : A quieter confidence is emerging.
            summary:
            You revisit old places but react differently now.
            The same symbols feel less threatening.
            invitation: Keep one small boundary today and notice your breath.
        """.trimIndent()

        val parsed = parseWeeklyInsight(raw)

        requireNotNull(parsed)
        assertEquals("A quieter confidence is emerging.", parsed.pattern)
        assertEquals(
            "You revisit old places but react differently now.\nThe same symbols feel less threatening.",
            parsed.summary,
        )
        assertEquals("Keep one small boundary today and notice your breath.", parsed.invitation)
    }

    @Test
    fun parseWeeklyInsight_returnsNullWhenRequiredFieldMissing() {
        val raw = """
            PATTERN: You are learning to stay.
            SUMMARY: You keep choosing steadiness over urgency.
        """.trimIndent()

        assertNull(parseWeeklyInsight(raw))
    }
}
