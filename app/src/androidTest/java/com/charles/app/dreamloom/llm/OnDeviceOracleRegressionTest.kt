package com.charles.app.dreamloom.llm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnDeviceOracleRegressionTest {

    @Test
    fun oracle_answer_is_readable_and_not_structured_garbage() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val engine = LlmEngine()
        engine.ensureLoaded(ModelStorage.modelFile(context))
        val responder = OracleResponder(engine)

        val answer = responder.answer(
            question = "Why do I keep dreaming I miss the same train?",
            topSymbols = listOf("water" to 4, "stairs" to 3, "mirror" to 2),
        ).trim()
        assertTrue("Oracle answer was too short: $answer", answer.length >= 50)
        assertTrue("Oracle leaked structured labels: $answer", !Regex("""(?im)^\s*(TITLE|SYMBOLS|INTERPRETATION|INTENTION)\s*:""").containsMatchIn(answer))
        assertTrue("Oracle looked unreadable: $answer", answer.any { it == ' ' } && answer.count { it.isLetter() } >= 35)
    }
}
