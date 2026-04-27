package com.charles.app.dreamloom.feature.atlas

import androidx.lifecycle.ViewModel
import com.charles.app.dreamloom.data.repo.DreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AtlasViewModel @Inject constructor(
    repo: DreamRepository,
) : ViewModel() {
    val dreams = repo.observeAll()
}
