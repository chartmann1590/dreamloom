package com.charles.app.dreamloom.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.charles.app.dreamloom.llm.ModelConfig
import com.charles.app.dreamloom.llm.ModelStorage
import androidx.work.Data
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

@HiltWorker
class ModelDownloadWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val httpClient: OkHttpClient,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val target = ModelStorage.modelFile(appContext)
        target.parentFile?.mkdirs()
        val tmp = File(target.parentFile!!, "${target.name}.part")
        val resumeFrom = if (tmp.exists()) tmp.length() else 0L
        for (url in ModelConfig.SOURCES) {
            val ok = runCatching { downloadWithResume(url, tmp, resumeFrom) }
                .getOrDefault(false)
            if (ok) {
                if (!verifySha256(tmp, ModelConfig.SHA256)) {
                    tmp.delete()
                    return@withContext Result.retry()
                }
                if (!tmp.renameTo(target)) {
                    tmp.copyTo(target, overwrite = true)
                    tmp.delete()
                }
                return@withContext Result.success()
            }
        }
        Result.retry()
    }

    private suspend fun downloadWithResume(url: String, dest: File, resumeFrom: Long): Boolean {
        val req = Request.Builder()
            .url(url)
            .apply { if (resumeFrom > 0) header("Range", "bytes=$resumeFrom-") }
            .build()
        httpClient.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return false
            val body = resp.body ?: return false
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
                    while (true) {
                        val n = input.read(buf)
                        if (n == -1) break
                        out.write(buf, 0, n)
                        written += n
                        setProgress(
                            Data.Builder()
                                .putLong("downloaded", written)
                                .putLong("total", total)
                                .build(),
                        )
                    }
                }
            }
            return true
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
