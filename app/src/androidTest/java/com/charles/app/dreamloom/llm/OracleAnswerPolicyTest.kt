package com.charles.app.dreamloom.llm

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OracleAnswerPolicyTest {

    @Test
    fun gibberish_is_rejected_and_fallback_is_acceptable() {
        val gibberish = "Theterersy"
        val cleaned = OracleAnswerPolicy.clean(gibberish)
        assertFalse(OracleAnswerPolicy.isAcceptable(cleaned))

        val fallback = OracleAnswerPolicy.fallback(
            question = "Why do I keep missing the same train?",
            topSymbols = listOf("water" to 4, "stairs" to 3),
        )
        assertTrue(OracleAnswerPolicy.isAcceptable(fallback))
    }
}
