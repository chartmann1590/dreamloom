package com.charles.app.dreamloom.feature.detail

import androidx.lifecycle.ViewModel
import com.charles.app.dreamloom.data.repo.DreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DreamDetailViewModel @Inject constructor(
    private val repo: DreamRepository,
) : ViewModel() {
    fun dream(id: Long) = repo.byId(id)
}
