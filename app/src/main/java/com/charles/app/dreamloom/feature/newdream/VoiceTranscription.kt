package com.charles.app.dreamloom.feature.newdream

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
/**
 * One session at a time: [startListening] / [stopListening] with partial + final text via [onUpdate].
 * Uses the platform [SpeechRecognizer]. Not thread-safe — call from main thread.
 */
class VoiceTranscription(
    private val appContext: Context,
    private val onUpdate: (displayText: String) -> Unit,
    private val onError: (String?) -> Unit,
    /** Milliseconds of silence before stopping; `0` disables auto-stop (Settings → Voice). */
    private val silenceAutoStopMs: Long = DefaultSilenceAutoStopMs,
) {
    private var recognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val finalSegments = mutableListOf<String>()
    private var partialTail: String = ""
    private var silenceEndRunnable: Runnable? = null
    private var suppressingErrors: Boolean = false
    private var sessionActive: Boolean = false
    private var restartRunnable: Runnable? = null
    private val autoStopMs: Long get() = silenceAutoStopMs

    private val listenIntent: Intent
        get() = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            // Ask the platform recognizer to allow longer speech windows before endpointing.
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 15_000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2_500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5_000L)
        }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = scheduleSilenceStop()
        override fun onBeginningOfSpeech() = scheduleSilenceStop()
        override fun onRmsChanged(rmsdB: Float) = scheduleSilenceStop()
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEndOfSpeech() = Unit
        override fun onEvent(eventType: Int, params: Bundle?) = Unit

        override fun onError(error: Int) {
            cancelSilenceStop()
            if (sessionActive && error != SpeechRecognizer.ERROR_CLIENT) {
                scheduleRecognizerRestart()
            }
            if (suppressingErrors) return
            if (sessionActive && error != SpeechRecognizer.ERROR_CLIENT) return
            val msg = when (error) {
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Listening timed out"
                SpeechRecognizer.ERROR_CLIENT -> "Voice unavailable"
                else -> null
            }
            onError(msg)
        }

        override fun onPartialResults(bundle: Bundle) {
            partialTail = bundle
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                .orEmpty()
            publish()
            scheduleSilenceStop()
        }

        override fun onResults(bundle: Bundle) {
            cancelSilenceStop()
            val b = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
            if (!b.isNullOrBlank()) {
                finalSegments.add(b.trim())
                partialTail = ""
            }
            publish()
            if (sessionActive) {
                scheduleRecognizerRestart()
            }
        }
    }

    val isAvailable: Boolean
        get() = SpeechRecognizer.isRecognitionAvailable(appContext)

    fun startListening() {
        cancelSilenceStop()
        cancelRecognizerRestart()
        partialTail = ""
        sessionActive = true
        val sr = recognizer ?: SpeechRecognizer.createSpeechRecognizer(appContext).also {
            it.setRecognitionListener(listener)
            recognizer = it
        }
        try {
            sr.startListening(listenIntent)
        } catch (_: Exception) {
            onError("Could not start listening")
        }
    }

    fun stopListening() {
        sessionActive = false
        cancelSilenceStop()
        cancelRecognizerRestart()
        suppressingErrors = true
        recognizer?.stopListening()
        mainHandler.postDelayed({ suppressingErrors = false }, 400L)
    }

    private fun autoStopFromSilence() {
        stopListening()
    }

    private fun scheduleRecognizerRestart() {
        cancelRecognizerRestart()
        if (!sessionActive) return
        val r = Runnable {
            if (!sessionActive) return@Runnable
            try {
                recognizer?.startListening(listenIntent)
            } catch (_: Exception) {
                onError("Could not continue listening")
            }
        }
        restartRunnable = r
        mainHandler.postDelayed(r, 200L)
    }

    private fun cancelRecognizerRestart() {
        restartRunnable?.let { mainHandler.removeCallbacks(it) }
        restartRunnable = null
    }

    private fun scheduleSilenceStop() {
        cancelSilenceStop()
        if (autoStopMs <= 0L) return
        val r = Runnable { autoStopFromSilence() }
        silenceEndRunnable = r
        mainHandler.postDelayed(r, autoStopMs)
    }

    private fun cancelSilenceStop() {
        silenceEndRunnable?.let { mainHandler.removeCallbacks(it) }
        silenceEndRunnable = null
    }

    fun release() {
        sessionActive = false
        cancelSilenceStop()
        cancelRecognizerRestart()
        recognizer?.destroy()
        recognizer = null
    }

    val combinedText: String
        get() = buildString {
            if (finalSegments.isNotEmpty()) {
                append(finalSegments.joinToString(" "))
            }
            if (partialTail.isNotEmpty()) {
                if (isNotEmpty()) append(" ")
                append(partialTail)
            }
        }

    private fun publish() {
        onUpdate(combinedText)
    }
}

private const val DefaultSilenceAutoStopMs: Long = 30_000L
