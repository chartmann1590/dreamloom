package com.charles.app.dreamloom.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.charles.app.dreamloom.data.db.InsightDao
import com.charles.app.dreamloom.data.db.InsightEntity
import com.charles.app.dreamloom.data.model.symbolIndexFromDreams
import com.charles.app.dreamloom.data.model.weekStartEpochDayForNow
import com.charles.app.dreamloom.data.prefs.AppPreferences
import com.charles.app.dreamloom.data.repo.DreamRepository
import com.charles.app.dreamloom.telemetry.DreamloomAnalytics
import com.charles.app.dreamloom.llm.GenParams
import com.charles.app.dreamloom.llm.LlmEngine
import com.charles.app.dreamloom.llm.ModelStorage
import com.charles.app.dreamloom.llm.parseWeeklyInsight
import com.charles.app.dreamloom.llm.prompts.InsightOraclePrompts
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withContext
import org.json.JSONObject

class WeeklyInsightWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface Dependencies {
        fun dreamRepository(): DreamRepository
        fun insightDao(): InsightDao
        fun engine(): LlmEngine
        fun analytics(): DreamloomAnalytics
        fun appPreferences(): AppPreferences
    }

    private val deps: Dependencies by lazy {
        EntryPointAccessors.fromApplication(applicationContext, Dependencies::class.java)
    }

    override suspend fun doWork(): Result = try {
        withContext(Dispatchers.Default) {
        deps.appPreferences().setWeeklyInsightStatus("running")
        val model = ModelStorage.modelFile(applicationContext)
        if (!model.exists()) {
            deps.appPreferences().setWeeklyInsightStatus("failed", "Model is not downloaded yet.")
            return@withContext Result.success()
        }
        val recent = deps.dreamRepository().latestInterpretedDreams(7)
        if (recent.isEmpty()) {
            deps.appPreferences().setWeeklyInsightStatus("failed", "Save and interpret at least one dream first.")
            return@withContext Result.success()
        }
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
        val prompt = InsightOraclePrompts.weeklyInsightUser(oldestFirst, top)
        val acc = StringBuilder()
        runCatching {
            deps.engine().ensureLoaded(model)
            deps.engine().generateStream(
                userMessage = prompt,
                systemPrompt = InsightOraclePrompts.WEEKLY_INSIGHT_SYSTEM,
                topK = GenParams.WEEKLY_INSIGHT_TOP_K,
                topP = GenParams.WEEKLY_INSIGHT_TOP_P.toDouble(),
                temperature = GenParams.WEEKLY_INSIGHT_TEMPERATURE.toDouble(),
                maxTokens = GenParams.WEEKLY_INSIGHT_MAX_TOKENS,
            ).collect { acc.append(it) }
        }.onFailure {
            deps.appPreferences().setWeeklyInsightStatus("failed", "Model generation failed. Try again.")
            return@withContext Result.retry()
        }
        val raw = acc.toString()
        val parsed = parseWeeklyInsight(raw) ?: buildFallbackInsight(oldestFirst, top)
        deps.insightDao().insert(
            InsightEntity(
                weekStartEpochDay = weekStart,
                createdAt = System.currentTimeMillis(),
                pattern = parsed.pattern,
                summary = parsed.summary,
                invitation = parsed.invitation,
                topSymbolsJson = topJson,
            ),
        )
        deps.appPreferences().setWeeklyInsightStatus("succeeded")
        deps.analytics().logWeeklyInsightGenerated()
        Result.success()
        }
    } finally {
        deps.appPreferences().setWeeklyInsightLastRunAt(System.currentTimeMillis())
    }

    private fun buildFallbackInsight(
        dreamsOldestFirst: List<com.charles.app.dreamloom.data.db.DreamEntity>,
        topSymbols: List<Pair<String, Int>>,
    ): com.charles.app.dreamloom.llm.WeeklyInsight {
        val moods = dreamsOldestFirst.map { it.mood.lowercase() }.distinct()
        val pattern = when {
            topSymbols.isNotEmpty() -> "Recurring symbols around ${topSymbols.first().first} keep returning with different emotional shades."
            moods.isNotEmpty() -> "Your recent dreams move through a ${moods.joinToString(", ")} emotional arc."
            else -> "A consistent inner thread is moving through your recent dreams."
        }
        val symbolsLine = if (topSymbols.isEmpty()) {
            "No stable symbol cluster has formed yet"
        } else {
            topSymbols.take(3).joinToString { it.first }
        }
        val summary = "Across this stretch, $symbolsLine appears beside shifting moods that still point to one theme: your system is trying to integrate what feels uncertain into something livable. The same motifs return, but with softer edges each time, suggesting gradual movement rather than a single breakthrough."
        val invitation = "Write one sentence tonight about the symbol or feeling that repeated most."
        return com.charles.app.dreamloom.llm.WeeklyInsight(pattern, summary, invitation)
    }
}
