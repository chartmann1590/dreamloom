package com.charles.app.dreamloom.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.app.dreamloom.data.prefs.AppPreferences
import com.charles.app.dreamloom.work.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val prefs: AppPreferences,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {
    val reminderEnabled = prefs.reminderEnabled.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        false,
    )
    val reminderHour = prefs.reminderHour.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        7,
    )
    val reminderMinute = prefs.reminderMinute.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        30,
    )
    val reminderText = prefs.reminderText.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        "Tap the moon. While the dream is still warm.",
    )

    fun setEnabled(enabled: Boolean) = viewModelScope.launch {
        prefs.setReminderEnabled(enabled)
        reminderScheduler.syncFromPreferences(prefs)
    }

    fun setTime(hour: Int, minute: Int) = viewModelScope.launch {
        prefs.setReminderTime(hour, minute)
        reminderScheduler.syncFromPreferences(prefs)
    }

    fun setReminderText(s: String) = viewModelScope.launch {
        prefs.setReminderText(s)
    }
}
