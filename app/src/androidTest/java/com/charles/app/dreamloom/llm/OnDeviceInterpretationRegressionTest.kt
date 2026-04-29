package com.charles.app.dreamloom.llm

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnDeviceInterpretationRegressionTest {

    @Test
    fun reinterpretation_is_not_identical_to_first_pass() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val engine = LlmEngine()
        val interpreter = DreamInterpreter(engine)

        engine.ensureLoaded(ModelStorage.modelFile(context))

        val dreamText = """
            I was walking through a flooded library at night while holding a cracked lantern.
            I found my childhood bedroom inside the library, and a fox was sleeping on my bed.
            I felt anxious but also strangely calm.
        """.trimIndent()

        val firstResult = collectResult {
            interpreter.interpret(
                dreamText = dreamText,
                mood = "anxious",
                photoFile = null,
            )
        }
        val first = firstResult.done
        assertNotNull("First interpretation was null. raw=${firstResult.lastRaw}", first)

        val secondResult = collectResult {
            interpreter.reInterpret(
                dreamText = dreamText,
                mood = "anxious",
                previousSymbols = first!!.symbols,
                photoFile = null,
            )
        }
        val second = secondResult.done
        assertNotNull("Re-interpretation was null. raw=${secondResult.lastRaw}", second)

        val overlap = first!!.symbols.toSet().intersect(second!!.symbols.toSet()).size
        val sameInterpretationText = first.interpretation.trim().equals(
            second.interpretation.trim(),
            ignoreCase = true,
        )

        assertTrue(
            "Re-interpretation stayed too similar. first=${first.symbols} second=${second.symbols} overlap=$overlap",
            overlap <= 2,
        )
        assertTrue(
            "Re-interpretation text was effectively identical",
            !sameInterpretationText,
        )
    }

    private data class RunResult(
        val done: Interpretation?,
        val lastRaw: String?,
    )

    private suspend fun collectResult(
        producer: suspend () -> kotlinx.coroutines.flow.Flow<InterpretChunk>,
    ): RunResult {
        var done: Interpretation? = null
        var lastRaw: String? = null
        producer().collect { chunk ->
            if (chunk is InterpretChunk.Done) done = chunk.interp
            if (chunk is InterpretChunk.Partial) lastRaw = chunk.raw
            if (chunk is InterpretChunk.Failed) lastRaw = chunk.raw
        }
        return RunResult(done, lastRaw)
    }
}
