package com.charles.app.dreamloom.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.app.dreamloom.data.WipeDataUseCase
import com.charles.app.dreamloom.data.prefs.AppPreferences
import com.charles.app.dreamloom.telemetry.Telemetry
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivacySettingsViewModel @Inject constructor(
    private val wipe: WipeDataUseCase,
    private val prefs: AppPreferences,
    @ApplicationContext private val app: Context,
) : ViewModel() {
    private val _wiping = MutableStateFlow(false)
    val wiping: StateFlow<Boolean> = _wiping.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val analyticsOptIn = prefs.analyticsOptIn.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        true,
    )
    val crashlyticsOptIn = prefs.crashlyticsOptIn.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        true,
    )

    fun setAnalyticsOptIn(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setAnalytics(enabled)
            Telemetry.apply(app, enabled, prefs.crashlyticsOptIn.first())
        }
    }

    fun setCrashlyticsOptIn(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setCrashlytics(enabled)
            Telemetry.apply(app, prefs.analyticsOptIn.first(), enabled)
        }
    }

    fun wipeAll() {
        viewModelScope.launch {
            _wiping.value = true
            _message.value = null
            runCatching { wipe.wipeAll() }
                .onFailure { t -> _message.value = t.message ?: "Wipe failed" }
            _wiping.value = false
        }
    }
}
