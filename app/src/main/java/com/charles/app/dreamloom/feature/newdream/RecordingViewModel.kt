package com.charles.app.dreamloom.feature.newdream

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.app.dreamloom.data.prefs.AppPreferences
import com.charles.app.dreamloom.data.repo.DreamRepository
import com.charles.app.dreamloom.telemetry.DreamloomAnalytics
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class RecordingViewModel @Inject constructor(
    @ApplicationContext private val app: Context,
    private val dreams: DreamRepository,
    private val analytics: DreamloomAnalytics,
    prefs: AppPreferences,
) : ViewModel() {
    val autoStopAfterSilence = prefs.autoStopAfterSilence.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        true,
    )
    var text: String = ""
    var mood: String = "skip"
    var audioPath: String? = null

    fun saveAndGo(pendingPhoto: Uri?, onId: (Long) -> Unit) {
        val id = System.currentTimeMillis()
        val t = text.trim()
        if (t.length < 10) return
        viewModelScope.launch {
            val path = pendingPhoto?.let { copyPhotoIntoDreamsDir(it, id) }
            dreams.create(
                id = id,
                rawText = t,
                mood = mood,
                photoPath = path,
                audioPath = audioPath,
            )
            analytics.logDreamSaved(hasPhoto = path != null, hasAudio = audioPath != null)
            onId(id)
        }
    }

    private suspend fun copyPhotoIntoDreamsDir(source: Uri, dreamId: Long): String? = withContext(Dispatchers.IO) {
        runCatching {
            val dir = File(app.filesDir, "dreams").apply { mkdirs() }
            val outFile = File(dir, "$dreamId.jpg")
            app.contentResolver.openInputStream(source)!!.use { input ->
                FileOutputStream(outFile).use { output -> input.copyTo(output) }
            }
            outFile.absolutePath
        }.getOrNull()
    }
}
