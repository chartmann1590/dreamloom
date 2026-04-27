package com.charles.app.dreamloom.data

import android.content.Context
import android.content.Intent
import com.charles.app.dreamloom.core.crypto.PassphraseProvider
import com.charles.app.dreamloom.data.db.AppDatabase
import com.charles.app.dreamloom.data.prefs.AppPreferences
import com.charles.app.dreamloom.llm.LlmEngine
import com.charles.app.dreamloom.llm.ModelStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WipeDataUseCase @Inject constructor(
    @ApplicationContext private val app: Context,
    private val pass: PassphraseProvider,
    private val prefs: AppPreferences,
    private val db: AppDatabase,
    private val engine: LlmEngine,
) {
    /**
     * Deletes local DB, model, DataStore, and encryption key, then restarts the process
     * so a fresh Room + passphrase can be created.
     */
    suspend fun wipeAll() {
        withContext(Dispatchers.IO) {
            engine.release()
            db.close()
            app.deleteDatabase("dreamloom.db")
            val model = ModelStorage.modelFile(app)
            if (model.exists()) model.delete()
            val modelsDir = File(app.filesDir, "models")
            if (modelsDir.isDirectory) modelsDir.listFiles()?.forEach { it.delete() }
        }
        prefs.clearAll()
        pass.clear()
        withContext(Dispatchers.Main) {
            val i = app.packageManager.getLaunchIntentForPackage(app.packageName)
                ?.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK,
                ) ?: return@withContext
            app.startActivity(i)
            android.os.Process.killProcess(android.os.Process.myPid())
        }
    }
}
