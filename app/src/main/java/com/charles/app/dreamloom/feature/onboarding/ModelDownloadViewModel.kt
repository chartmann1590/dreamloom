package com.charles.app.dreamloom.feature.onboarding

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.charles.app.dreamloom.llm.ModelConfig
import com.charles.app.dreamloom.work.ModelDownloadWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ModelDownloadViewModel @Inject constructor() : ViewModel() {
    private val _progress = mutableStateOf<Pair<Long, Long>?>(null)
    val progress: State<Pair<Long, Long>?> = _progress

    fun startDownload(context: Context) {
        val work = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresStorageNotLow(true)
                    .setRequiresBatteryNotLow(true)
                    .build(),
            )
            .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork("model_download_${ModelConfig.VERSION}", ExistingWorkPolicy.REPLACE, work)
    }
}
