package com.charles.app.dreamloom.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.app.dreamloom.data.prefs.AppPreferences
import com.charles.app.dreamloom.data.prefs.ThemeMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ShellViewModel @Inject constructor(
    prefs: AppPreferences,
) : ViewModel() {
    val themeMode = prefs.themeMode.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        ThemeMode.Dark,
    )
}
