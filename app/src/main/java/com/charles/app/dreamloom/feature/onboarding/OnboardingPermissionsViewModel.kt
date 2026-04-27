package com.charles.app.dreamloom.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.app.dreamloom.data.prefs.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingPermissionsViewModel @Inject constructor(
    private val prefs: AppPreferences,
) : ViewModel() {
    fun markOnboardingComplete() {
        viewModelScope.launch {
            prefs.setOnboardingComplete(true)
        }
    }
}
