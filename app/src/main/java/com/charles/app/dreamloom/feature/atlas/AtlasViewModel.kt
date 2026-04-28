package com.charles.app.dreamloom.feature.atlas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.app.dreamloom.data.db.DreamEntity
import com.charles.app.dreamloom.data.model.buildFtsMatchQuery
import com.charles.app.dreamloom.data.model.hasSymbol
import com.charles.app.dreamloom.data.model.startOfThisMonthMs
import com.charles.app.dreamloom.data.model.startOfThisWeekMs
import com.charles.app.dreamloom.data.model.symbolIndexFromDreams
import com.charles.app.dreamloom.data.repo.DreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class AtlasViewModel @Inject constructor(
    private val repo: DreamRepository,
) : ViewModel() {
    val searchText = MutableStateFlow("")

    val timeFilter = MutableStateFlow(AtlasTimeFilter.All)
    val symbolFilter = MutableStateFlow<String?>(null)
    val moodFilter = MutableStateFlow<String?>(null)

    private val debouncedSearch: StateFlow<String> = searchText
        .map { it.trim() }
        .debounce(220L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    val filteredDreams: StateFlow<List<DreamEntity>> = combine(
        debouncedSearch.flatMapLatest { q ->
            val match = buildFtsMatchQuery(q)
            if (match == null) {
                repo.observeAll()
            } else {
                repo.searchFts(match)
            }
        },
        timeFilter,
        symbolFilter,
        moodFilter,
    ) { list, t, sym, m ->
        var r: List<DreamEntity> = list
        when (t) {
            AtlasTimeFilter.All -> Unit
            AtlasTimeFilter.Week -> {
                val start = startOfThisWeekMs()
                r = r.filter { it.createdAt >= start }
            }
            AtlasTimeFilter.Month -> {
                val start = startOfThisMonthMs()
                r = r.filter { it.createdAt >= start }
            }
        }
        if (sym != null) {
            r = r.filter { it.hasSymbol(sym) }
        }
        if (m != null) {
            r = r.filter { it.mood.equals(m, ignoreCase = true) }
        }
        r
    }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList(),
        )

    val allDreams: StateFlow<List<DreamEntity>> = repo
        .observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val symbolIndex: StateFlow<List<Pair<String, Int>>> = allDreams
        .map { symbolIndexFromDreams(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteDream(id: Long) = viewModelScope.launch {
        repo.delete(id)
    }

    suspend fun getDream(id: Long) = repo.getById(id)
}
