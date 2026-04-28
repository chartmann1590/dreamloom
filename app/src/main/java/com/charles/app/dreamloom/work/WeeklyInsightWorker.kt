package com.charles.app.dreamloom.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.charles.app.dreamloom.data.db.InsightDao
import com.charles.app.dreamloom.data.db.InsightEntity
import com.charles.app.dreamloom.data.model.symbolIndexFromDreams
import com.charles.app.dreamloom.data.model.weekStartEpochDayForNow
import com.charles.app.dreamloom.data.repo.DreamRepository
import com.charles.app.dreamloom.telemetry.DreamloomAnalytics
import com.charles.app.dreamloom.llm.GenParams
import com.charles.app.dreamloom.llm.LlmEngine
import com.charles.app.dreamloom.llm.ModelStorage
import com.charles.app.dreamloom.llm.parseWeeklyInsight
import com.charles.app.dreamloom.llm.prompts.InsightOraclePrompts
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import org.json.JSONObject

@HiltWorker
class WeeklyInsightWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val dreamRepository: DreamRepository,
    private val insightDao: InsightDao,
    private val engine: LlmEngine,
    private val analytics: DreamloomAnalytics,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = withContext(Dispatchers.Default) {
        val model = ModelStorage.modelFile(appContext)
        if (!model.exists()) {
            return@withContext Result.success()
        }
        val recent = dreamRepository.latestInterpretedDreams(7)
        if (recent.isEmpty()) return@withContext Result.success()
        val oldestFirst = recent.sortedBy { it.createdAt }
        val top = symbolIndexFromDreams(recent).take(5)
        val topJson = buildString {
            append('[')
            top.forEachIndexed { i, (s, c) ->
                if (i > 0) append(',')
                append(JSONObject().put("s", s).put("c", c).toString())
            }
            append(']')
        }
        val weekStart = weekStartEpochDayForNow()
        val prompt = InsightOraclePrompts.weeklyInsightGemmaChat(oldestFirst, top)
        val acc = StringBuilder()
        runCatching {
            engine.ensureLoaded(model)
            engine.generateStream(
                userMessage = prompt,
                topK = GenParams.WEEKLY_INSIGHT_TOP_K,
                topP = GenParams.WEEKLY_INSIGHT_TOP_P.toDouble(),
                temperature = GenParams.WEEKLY_INSIGHT_TEMPERATURE.toDouble(),
                maxTokens = GenParams.WEEKLY_INSIGHT_MAX_TOKENS,
            ).collect { acc.append(it) }
        }.onFailure { return@withContext Result.retry() }
        val raw = acc.toString()
        val parsed = parseWeeklyInsight(raw) ?: return@withContext Result.success()
        insightDao.insert(
            InsightEntity(
                weekStartEpochDay = weekStart,
                createdAt = System.currentTimeMillis(),
                pattern = parsed.pattern,
                summary = parsed.summary,
                invitation = parsed.invitation,
                topSymbolsJson = topJson,
            ),
        )
        analytics.logWeeklyInsightGenerated()
        Result.success()
    }
}
