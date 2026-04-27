package com.charles.app.dreamloom.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.app.dreamloom.data.WipeDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrivacySettingsViewModel @Inject constructor(
    private val wipe: WipeDataUseCase,
) : ViewModel() {
    private val _wiping = MutableStateFlow(false)
    val wiping: StateFlow<Boolean> = _wiping.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

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
