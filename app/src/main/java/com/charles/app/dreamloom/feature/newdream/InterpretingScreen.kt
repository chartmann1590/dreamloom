package com.charles.app.dreamloom.feature.newdream

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.charles.app.dreamloom.ui.theme.DreamSpacing

@Composable
fun InterpretingScreen(
    dreamId: Long,
    onComplete: (Long) -> Unit,
    vm: InterpretingViewModel = hiltViewModel(),
) {
    val raw by vm.raw.collectAsState()
    val done by vm.done.collectAsState()
    LaunchedEffect(done) {
        if (done) onComplete(dreamId)
    }
    Column(Modifier.fillMaxSize().padding(DreamSpacing.lg)) {
        Text("Weaving…")
        Text(raw)
    }
}
