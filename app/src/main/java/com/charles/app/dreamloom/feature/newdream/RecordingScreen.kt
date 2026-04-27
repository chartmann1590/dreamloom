package com.charles.app.dreamloom.feature.newdream

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.charles.app.dreamloom.ui.theme.DreamSpacing

@Composable
fun RecordingScreen(
    onBack: () -> Unit,
    onInterpreting: (Long) -> Unit,
    vm: RecordingViewModel = hiltViewModel(),
) {
    val line = remember { mutableStateOf(vm.text) }
    Column(Modifier.fillMaxSize().padding(DreamSpacing.lg)) {
        Text("New dream")
        OutlinedTextField(
            value = line.value,
            onValueChange = { line.value = it; vm.text = it },
            label = { Text("I dreamt of…") },
        )
        Button(
            onClick = {
                vm.text = line.value
                vm.saveAndGo { id -> onInterpreting(id) }
            },
        ) { Text("Save dream") }
        Button(onClick = onBack) { Text("Back") }
    }
}
