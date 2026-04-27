package com.charles.app.dreamloom.feature.newdream

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.app.dreamloom.data.repo.DreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordingViewModel @Inject constructor(
    private val dreams: DreamRepository,
) : ViewModel() {
    var text: String = ""
    var mood: String = "skip"
    var photoPath: String? = null
    var audioPath: String? = null

    fun saveAndGo(onId: (Long) -> Unit) {
        val id = System.currentTimeMillis()
        val t = text.trim()
        if (t.length < 10) return
        viewModelScope.launch {
            dreams.create(
                id = id,
                rawText = t,
                mood = mood,
                photoPath = photoPath,
                audioPath = audioPath,
            )
            onId(id)
        }
    }
}
