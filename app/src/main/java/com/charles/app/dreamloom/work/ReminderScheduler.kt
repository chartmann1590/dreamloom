package com.charles.app.dreamloom.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.charles.app.dreamloom.data.prefs.AppPreferences
import com.charles.app.dreamloom.notification.ReminderNotifications
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val appContext: Context,
) {
    private val workManager: WorkManager
        get() = WorkManager.getInstance(appContext)

    suspend fun syncFromPreferences(prefs: AppPreferences) {
        val enabled = prefs.reminderEnabled.first()
        if (!enabled) {
            workManager.cancelUniqueWork(UNIQUE_NAME)
            return
        }
        val hour = prefs.reminderHour.first()
        val minute = prefs.reminderMinute.first()
        val initial = millisUntilNext(hour, minute, ZoneId.systemDefault())
        ReminderNotifications.ensureChannel(appContext)
        val work = PeriodicWorkRequestBuilder<ReminderWorker>(REPEAT_DAYS, TimeUnit.DAYS)
            .setInitialDelay(initial, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build(),
            )
            .build()
        workManager.enqueueUniquePeriodicWork(
            UNIQUE_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            work,
        )
    }

    companion object {
        private const val UNIQUE_NAME = "dreamloom_reminder"
        private const val REPEAT_DAYS = 1L
    }
}

private fun millisUntilNext(hour: Int, minute: Int, zone: ZoneId): Long {
    val now = ZonedDateTime.now(zone)
    var next = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
    if (!next.isAfter(now)) {
        next = next.plusDays(1)
    }
    return ChronoUnit.MILLIS.between(now, next)
}
