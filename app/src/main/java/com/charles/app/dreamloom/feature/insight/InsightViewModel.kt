package com.charles.app.dreamloom.feature.insight

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.app.dreamloom.ads.RewardedAdManager
import com.charles.app.dreamloom.ads.RewardedPlacement
import com.charles.app.dreamloom.data.prefs.AppPreferences
import com.charles.app.dreamloom.data.db.InsightDao
import com.charles.app.dreamloom.data.db.InsightEntity
import com.charles.app.dreamloom.data.db.DreamEntity
import com.charles.app.dreamloom.data.repo.DreamRepository
import com.charles.app.dreamloom.work.WeeklyInsightWorkScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InsightViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    insightDao: InsightDao,
    private val dreamRepo: DreamRepository,
    private val appPreferences: AppPreferences,
    private val rewardedAdManager: RewardedAdManager,
) : ViewModel() {
    data class InsightRunStatus(
        val isRunning: Boolean,
        val note: String?,
    )

    val latestInsight: StateFlow<InsightEntity?> = insightDao
        .observeAll()
        .map { it.maxByOrNull { row -> row.createdAt } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Newest 7 interpreted dreams, oldest first — pairs with the weekly card mood strip. */
    val recentStripDreams: StateFlow<List<DreamEntity>> = dreamRepo
        .observeAll()
        .map { dreams ->
            dreams
                .filter { it.isInterpretationComplete }
                .sortedByDescending { it.createdAt }
                .take(7)
                .reversed()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val lastRunAtMs: StateFlow<Long?> = appPreferences.weeklyInsightLastRunAt
        .map { ts -> ts.takeIf { it > 0L } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val nextRunAtMs: StateFlow<Long> = appPreferences.weeklyInsightLastRunAt
        .map { WeeklyInsightWorkScheduler.nextScheduledAtEpochMs() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WeeklyInsightWorkScheduler.nextScheduledAtEpochMs())

    val runStatus: StateFlow<InsightRunStatus> = combine(
        appPreferences.weeklyInsightStatus.map { it.trim().lowercase() },
        appPreferences.weeklyInsightStatusNote,
    ) { status, note ->
        when (status) {
            "running" -> InsightRunStatus(isRunning = true, note = "Running weekly insight now...")
            "failed" -> InsightRunStatus(isRunning = false, note = note.ifBlank { "Weekly insight could not be generated." })
            "succeeded" -> InsightRunStatus(isRunning = false, note = "Weekly insight updated.")
            else -> InsightRunStatus(isRunning = false, note = null)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), InsightRunStatus(false, null))

    fun runNow() {
        viewModelScope.launch {
            appPreferences.setWeeklyInsightStatus("running")
            WeeklyInsightWorkScheduler.runNow(appContext)
        }
    }

    fun preloadRewarded() {
        rewardedAdManager.preload(RewardedPlacement.WeeklyInsight)
    }

    fun showWeeklyRewarded(
        activity: android.app.Activity,
        onAfter: (rewardEarned: Boolean) -> Unit,
    ) {
        rewardedAdManager.show(
            activity = activity,
            placement = RewardedPlacement.WeeklyInsight,
            onFinished = onAfter,
        )
    }
}
