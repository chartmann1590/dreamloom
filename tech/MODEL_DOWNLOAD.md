# Model Download

The Gemma 4 E2B model file is ~2.58 GB. We must NOT bundle it in the AAB (Play Store AAB cap is 200 MB; even 1 GB Asset Delivery wastes bandwidth on users who never finish onboarding). We download it once on first launch via `WorkManager`.

---

## Hosting

**GitHub Releases** under the project repo, e.g.:

```
https://github.com/<user>/dreamloom-android/releases/download/model-v1/gemma-4-E2B-it-int4.litertlm
```

Why GitHub Releases:
- Straightforward public hosting for release assets
- Generous bandwidth for public releases; suitable CDN behavior for large files
- Resumable via HTTP `Range` headers (GitHub's CDN supports this)
- Versionable via release tags — ship `model-v2` if you re-quantize

Backup: **Cloudflare R2** (or similar object storage) as a secondary mirror in `SOURCES` — if the primary returns 403/429, fall back automatically.

The release tag MUST be immutable. Never overwrite `model-v1`. New model = new tag, app config bumps.

---

## App-side constants

In `app/src/main/java/app/dreamloom/llm/ModelConfig.kt`:

```kotlin
object ModelConfig {
    const val VERSION = "v1"
    const val FILENAME = "gemma-4-E2B-it-int4.litertlm"
    const val EXPECTED_SIZE_BYTES = 2_770_000_000L  // ~2.58 GiB; tweak after first build
    const val SHA256 = "REPLACE_AT_BUILD_TIME"

    val SOURCES = listOf(
        "https://github.com/<user>/dreamloom-android/releases/download/model-$VERSION/$FILENAME",
        "https://r2.dreamloom.app/model-$VERSION/$FILENAME",
    )
}
```

The SHA-256 is recorded once during build of the model release. Verifying it on download prevents corruption and prevents a malicious mirror from swapping the model.

---

## Storage location

```kotlin
object ModelStorage {
    fun modelFile(ctx: Context): File =
        File(ctx.filesDir, "models/${ModelConfig.FILENAME}")
}
```

`filesDir` is app-private internal storage:
- Survives app updates
- Cleared on app uninstall (which is what we want for the privacy promise)
- Backed up via Android Auto-Backup unless we exclude it (we do — `android:fullBackupContent` rule excludes the model dir; backing up 2.6 GB is wasteful)

`AndroidManifest.xml` excerpt:
```xml
<application
    android:allowBackup="true"
    android:dataExtractionRules="@xml/data_extraction_rules"
    android:fullBackupContent="@xml/backup_rules"
    ...>
```

`res/xml/backup_rules.xml`:
```xml
<full-backup-content>
    <exclude domain="file" path="models/" />
</full-backup-content>
```

---

## ModelDownloadWorker

```kotlin
@HiltWorker
class ModelDownloadWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val httpClient: OkHttpClient,
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        val target = ModelStorage.modelFile(applicationContext)
        target.parentFile?.mkdirs()
        val tmp = File(target.parentFile, "${target.name}.part")

        // Try sources in order; allow resume from .part
        val resumeFrom = if (tmp.exists()) tmp.length() else 0L

        for (url in ModelConfig.SOURCES) {
            val ok = runCatching { downloadWithResume(url, tmp, resumeFrom) }
                .getOrDefault(false)
            if (ok) {
                if (!verifySha256(tmp, ModelConfig.SHA256)) {
                    tmp.delete()
                    return Result.retry()
                }
                tmp.renameTo(target)
                return Result.success()
            }
        }
        return Result.retry()
    }

    private suspend fun downloadWithResume(
        url: String, dest: File, resumeFrom: Long,
    ): Boolean = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url(url)
            .apply { if (resumeFrom > 0) header("Range", "bytes=$resumeFrom-") }
            .build()

        httpClient.newCall(req).execute().use { resp ->
            if (!resp.isSuccessful) return@use false
            val body = resp.body ?: return@use false
            val total = (body.contentLength() + resumeFrom)
                .takeIf { it > 0 } ?: ModelConfig.EXPECTED_SIZE_BYTES

            FileOutputStream(dest, /* append = */ resumeFrom > 0).use { out ->
                body.byteStream().use { input ->
                    val buf = ByteArray(64 * 1024)
                    var written = resumeFrom
                    while (currentCoroutineContext().isActive) {
                        val n = input.read(buf)
                        if (n == -1) break
                        out.write(buf, 0, n)
                        written += n
                        setProgress(workDataOf(
                            "downloaded" to written,
                            "total" to total,
                        ))
                    }
                }
            }
            return@use dest.length() == total
        }
    }

    private fun verifySha256(file: File, expected: String): Boolean {
        if (expected == "REPLACE_AT_BUILD_TIME") return true  // dev mode
        val md = MessageDigest.getInstance("SHA-256")
        file.inputStream().use {
            val buf = ByteArray(1 shl 16)
            while (true) {
                val n = it.read(buf)
                if (n == -1) break
                md.update(buf, 0, n)
            }
        }
        val hex = md.digest().joinToString("") { "%02x".format(it) }
        return hex.equals(expected, ignoreCase = true)
    }
}
```

---

## Scheduling rules

```kotlin
fun enqueueModelDownload(ctx: Context, wifiOnly: Boolean) {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(if (wifiOnly) NetworkType.UNMETERED else NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .setRequiresStorageNotLow(true)
        .build()

    val request = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
        .setConstraints(constraints)
        .setBackoffCriteria(BackoffPolicy.LINEAR, 30, TimeUnit.SECONDS)
        .build()

    WorkManager.getInstance(ctx)
        .enqueueUniqueWork("model_download_${ModelConfig.VERSION}",
                           ExistingWorkPolicy.KEEP, request)
}
```

Notes:
- `setRequiresStorageNotLow(true)` — Android cancels if storage is low. We surface a friendly error in onboarding when this fires.
- `setRequiresBatteryNotLow(true)` — saves us angry reviews from users whose phones died mid-download.
- `BackoffPolicy.LINEAR, 30s` — retries gently rather than hammering.
- The unique-work name includes the version, so a `model-v2` release scheduled later doesn't collide.

---

## Observing progress in onboarding UI

```kotlin
@Composable
fun ModelDownloadScreen(...) {
    val info by WorkManager.getInstance(ctx)
        .getWorkInfosForUniqueWorkLiveData("model_download_${ModelConfig.VERSION}")
        .observeAsState(emptyList())
    val current = info.firstOrNull()

    val downloaded = current?.progress?.getLong("downloaded", 0L) ?: 0L
    val total = current?.progress?.getLong("total", ModelConfig.EXPECTED_SIZE_BYTES)
        ?: ModelConfig.EXPECTED_SIZE_BYTES
    val pct = (downloaded.toDouble() / total).coerceIn(0.0, 1.0)

    when (current?.state) {
        WorkInfo.State.SUCCEEDED -> NavigateToHome()
        WorkInfo.State.FAILED    -> ShowRetry()
        else                     -> ProgressUi(pct, downloaded, total)
    }
}
```

Format MB with `Formatter.formatShortFileSize(ctx, bytes)`.

---

## Error UX

| Failure | UI |
| --- | --- |
| No internet | "Connect to Wi-Fi to bring the dream-reader home." |
| Cellular but Wi-Fi-only mode on | "Waiting for Wi-Fi…" with a "Use cellular instead" button |
| Storage full | "We need 3 GB of free space. Clear some up and try again." |
| SHA-256 mismatch | "Something went wrong with the download. Tap to start again." (silent retry from primary mirror's start; if primary fails twice, switch to backup) |
| Repeated failure | After 5 attempts: surface contact email + a manual "open in browser" link to the GitHub Release as a last resort. |

---

## Re-download path

In `Settings → About`, hidden behind a long-press on the model version label (debug-friendly), expose "Re-download model". This deletes the existing file + enqueues the worker again. Useful when SDK updates ship a newer compatible model.

---

## Privacy: what does the download server see?

Just an HTTP GET. No user identifier. We do NOT add any custom headers, query params, install IDs, or analytics tied to the download. The Privacy section of the listing can honestly say "the only network call we make for journal data is downloading the AI model, which is the same file for every user."
