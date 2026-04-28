package com.charles.app.dreamloom.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.app.dreamloom.data.prefs.AppPreferences
import com.charles.app.dreamloom.data.prefs.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsRootViewModel @Inject constructor(
    private val prefs: AppPreferences,
) : ViewModel() {
    val themeMode = prefs.themeMode.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ThemeMode.Dark,
    )
    val autoStopAfterSilence = prefs.autoStopAfterSilence.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        true,
    )

    fun setTheme(mode: ThemeMode) {
        viewModelScope.launch { prefs.setTheme(mode) }
    }

    fun setAutoStopAfterSilence(enabled: Boolean) {
        viewModelScope.launch { prefs.setAutoStop(enabled) }
    }
}
