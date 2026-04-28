package com.charles.app.dreamloom.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.charles.app.dreamloom.data.prefs.AppPreferences
import com.charles.app.dreamloom.notification.ReminderNotifications
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted params: WorkerParameters,
    private val prefs: AppPreferences,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        if (!prefs.reminderEnabled.first()) {
            return Result.success()
        }
        val text = prefs.reminderText.first()
        ReminderNotifications.showReminder(appContext, text)
        return Result.success()
    }
}
