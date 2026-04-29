package com.charles.app.dreamloom.work

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit
object WeeklyInsightWorkScheduler {
    const val PERIODIC_NAME = "weekly_insight"
    const val MANUAL_NOW_NAME = "weekly_insight_manual_now"

    fun schedule(context: Context) {
        val initial = initialDelayToNextSunday9amMs()
        val req = PeriodicWorkRequestBuilder<WeeklyInsightWorker>(7, TimeUnit.DAYS)
            .setInitialDelay(initial, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            req,
        )
    }

    fun runNow(context: Context) {
        val req = OneTimeWorkRequestBuilder<WeeklyInsightWorker>().build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            MANUAL_NOW_NAME,
            ExistingWorkPolicy.REPLACE,
            req,
        )
    }

    fun nextScheduledAtEpochMs(
        now: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault()),
    ): Long = now.toInstant().toEpochMilli() + initialDelayToNextSunday9amMs(now)

    /** Next Sunday 9:00 local, strictly after [now] if the model must not run in the same minute. */
    fun initialDelayToNextSunday9amMs(
        now: ZonedDateTime = ZonedDateTime.now(ZoneId.systemDefault()),
    ): Long {
        val z = now.zone
        for (i in 0L..21L) {
            val d = now.toLocalDate().plusDays(i)
            if (d.dayOfWeek == DayOfWeek.SUNDAY) {
                var candidate = d.atTime(9, 0).atZone(z)
                if (candidate.isAfter(now)) {
                    return java.time.Duration.between(now, candidate).toMillis()
                }
            }
        }
        // Fallback: one week
        return 7L * 24 * 60 * 60 * 1000L
    }
}
