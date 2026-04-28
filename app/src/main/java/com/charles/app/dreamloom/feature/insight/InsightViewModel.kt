package com.charles.app.dreamloom.feature.insight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.app.dreamloom.data.db.InsightDao
import com.charles.app.dreamloom.data.db.InsightEntity
import com.charles.app.dreamloom.data.db.DreamEntity
import com.charles.app.dreamloom.data.repo.DreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class InsightViewModel @Inject constructor(
    insightDao: InsightDao,
    private val dreamRepo: DreamRepository,
) : ViewModel() {
    val latestInsight: StateFlow<InsightEntity?> = insightDao
        .observeAll()
        .map { it.maxByOrNull { row -> row.createdAt } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    /** Newest 7 interpreted dreams, oldest first — pairs with the weekly card mood strip. */
    val recentStripDreams: StateFlow<List<DreamEntity>> = dreamRepo
        .observeAll()
        .map { dreams ->
            dreams
                .filter { it.isInterpretationComplete }
                .sortedByDescending { it.createdAt }
                .take(7)
                .reversed()
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
