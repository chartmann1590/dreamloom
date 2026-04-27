package com.charles.app.dreamloom.feature.detail

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun DreamDetailScreen(
    id: Long,
    onBack: () -> Unit,
    vm: DreamDetailViewModel = hiltViewModel(),
) {
    val d by vm.dream(id).collectAsState(initial = null)
    Text(d?.title ?: "…")
}
