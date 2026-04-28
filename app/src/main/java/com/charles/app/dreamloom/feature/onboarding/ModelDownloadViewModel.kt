package com.charles.app.dreamloom.feature.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.charles.app.dreamloom.llm.ModelConfig
import com.charles.app.dreamloom.work.ModelDownloadWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ModelDownloadViewModel @Inject constructor(
    @ApplicationContext private val app: Context,
) : ViewModel() {

    enum class Phase { Idle, Queued, Running, Succeeded, Failed, Blocked }

    data class UiState(
        val phase: Phase,
        val downloaded: Long,
        val total: Long,
    )

    private val workName = "model_download_${ModelConfig.VERSION}"
    private val workManager = WorkManager.getInstance(app)

    val state: StateFlow<UiState> = workManager
        .getWorkInfosForUniqueWorkFlow(workName)
        .map { infos -> infos.firstOrNull().toUiState() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState(Phase.Idle, 0L, 0L),
        )

    fun startDownload() {
        workManager.enqueueUniqueWork(workName, ExistingWorkPolicy.KEEP, buildRequest())
    }

    fun retry() {
        workManager.enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, buildRequest())
    }

    private fun buildRequest() = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresStorageNotLow(true)
                .build(),
        )
        .build()

    private fun WorkInfo?.toUiState(): UiState {
        if (this == null) return UiState(Phase.Idle, 0L, 0L)
        val downloaded = progress.getLong("downloaded", 0L)
        val total = progress.getLong("total", 0L)
        val phase = when (state) {
            WorkInfo.State.ENQUEUED -> Phase.Queued
            WorkInfo.State.RUNNING -> Phase.Running
            WorkInfo.State.SUCCEEDED -> Phase.Succeeded
            WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> Phase.Failed
            WorkInfo.State.BLOCKED -> Phase.Blocked
        }
        return UiState(phase, downloaded, total)
    }
}
