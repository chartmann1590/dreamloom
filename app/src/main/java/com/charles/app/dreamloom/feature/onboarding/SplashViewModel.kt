package com.charles.app.dreamloom.feature.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.app.dreamloom.data.prefs.AppPreferences
import com.charles.app.dreamloom.llm.ModelStorage
import com.charles.app.dreamloom.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    @ApplicationContext private val app: Context,
    private val prefs: AppPreferences,
) : ViewModel() {
    fun nextDestination(): String = runBlocking {
        val done = prefs.onboardingComplete.first()
        if (!done) {
            return@runBlocking Routes.WELCOME
        }
        val model: File = ModelStorage.modelFile(app)
        if (!model.exists() || model.length() < 1024L * 1024L) {
            return@runBlocking Routes.MODEL_DOWNLOAD
        }
        Routes.HOME
    }
}
