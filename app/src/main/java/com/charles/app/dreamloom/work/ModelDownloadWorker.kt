package com.charles.app.dreamloom.work

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.charles.app.dreamloom.llm.ModelConfig
import com.charles.app.dreamloom.llm.ModelStorage
import com.charles.app.dreamloom.telemetry.DreamloomAnalytics
import androidx.work.Data
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

class ModelDownloadWorker constructor(
    private val appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    companion object {
        private const val TAG = "ModelDownloadWorker"
    }
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkerDeps {
        fun okHttpClient(): OkHttpClient
        fun analytics(): DreamloomAnalytics
    }

    private val deps by lazy {
        EntryPointAccessors.fromApplication(appContext, WorkerDeps::class.java)
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val target = ModelStorage.modelFile(appContext)
        target.parentFile?.mkdirs()
        val tmp = File(target.parentFile!!, "${target.name}.part")
        var anyTransientFailure = false
        for (url in ModelConfig.SOURCES) {
            val resumeFrom = if (tmp.exists()) tmp.length() else 0L
            val outcome = runCatching { downloadWithResume(url, tmp, resumeFrom) }
                .onFailure { e ->
                    Log.e(TAG, "Download failed from $url", e)
                    anyTransientFailure = true
                }
                .getOrDefault(DownloadOutcome.HardFail)
            when (outcome) {
                DownloadOutcome.Success -> {
                    if (!verifySha256(tmp, ModelConfig.modelSha256)) {
                        Log.e(TAG, "SHA-256 mismatch for downloaded model")
                        tmp.delete()
                        return@withContext Result.failure()
                    }
                    if (!tmp.renameTo(target)) {
                        tmp.copyTo(target, overwrite = true)
                        tmp.delete()
                    }
                    deps.analytics().logModelDownloadCompleted(ModelConfig.VERSION)
                    return@withContext Result.success()
                }
                DownloadOutcome.HardFail -> Unit
                DownloadOutcome.Transient -> { anyTransientFailure = true }
            }
        }
        // Exhausted every mirror. Retry only on transient/network errors,
        // and at most a few times — otherwise surface a real failure to the UI.
        return@withContext if (anyTransientFailure && runAttemptCount < 3) {
            Log.w(TAG, "All sources failed transiently (attempt $runAttemptCount); retrying")
            Result.retry()
        } else {
            Log.e(TAG, "All sources failed; reporting failure to UI")
            Result.failure()
        }
    }

    private enum class DownloadOutcome { Success, HardFail, Transient }

    private suspend fun downloadWithResume(url: String, dest: File, resumeFrom: Long): DownloadOutcome {
        val req = Request.Builder()
            .url(url)
            .apply { if (resumeFrom > 0) header("Range", "bytes=$resumeFrom-") }
            .build()
        deps.okHttpClient().newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) {
                Log.w(TAG, "HTTP ${resp.code} from $url")
                // 4xx => the URL itself is wrong; don't retry forever.
                // 5xx => server-side glitch; treat as transient.
                return if (resp.code in 400..499) DownloadOutcome.HardFail else DownloadOutcome.Transient
            }
            val body = resp.body ?: return DownloadOutcome.Transient
            val contentLength = body.contentLength()
            val total = when {
                contentLength > 0 && resumeFrom > 0 -> contentLength + resumeFrom
                contentLength > 0 -> contentLength
                else -> ModelConfig.EXPECTED_SIZE_BYTES
            }
            FileOutputStream(dest, resumeFrom > 0).use { out ->
                body.byteStream().use { input ->
                    val buf = ByteArray(64 * 1024)
                    var written = resumeFrom
                    var lastReport = 0L
                    var bytesSinceReport = 0L
                    while (true) {
                        val n = input.read(buf)
                        if (n == -1) break
                        out.write(buf, 0, n)
                        written += n
                        bytesSinceReport += n
                        val now = System.currentTimeMillis()
                        if (bytesSinceReport >= 1_000_000L || now - lastReport >= 250L) {
                            setProgress(
                                Data.Builder()
                                    .putLong("downloaded", written)
                                    .putLong("total", total)
                                    .build(),
                            )
                            lastReport = now
                            bytesSinceReport = 0L
                        }
                    }
                    setProgress(
                        Data.Builder()
                            .putLong("downloaded", written)
                            .putLong("total", total)
                            .build(),
                    )
                }
            }
            return DownloadOutcome.Success
        }
    }

    private fun verifySha256(file: File, expected: String): Boolean {
        if (expected == "REPLACE_AT_BUILD_TIME") return true
        val md = MessageDigest.getInstance("SHA-256")
        file.inputStream().use {
            val buf = ByteArray(1 shl 16)
            while (true) {
                val n = it.read(buf)
                if (n == -1) break
                md.update(buf, 0, n)
            }
        }
        val hex = md.digest().joinToString("") { b -> "%02x".format(b) }
        return hex.equals(expected, ignoreCase = true)
    }
}
