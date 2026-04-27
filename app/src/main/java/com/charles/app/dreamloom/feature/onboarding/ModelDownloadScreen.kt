package com.charles.app.dreamloom.feature.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.charles.app.dreamloom.ui.theme.DreamSpacing

@Composable
fun ModelDownloadScreen(
    onDone: () -> Unit,
    vm: ModelDownloadViewModel = hiltViewModel(),
) {
    val p = vm.progress.value
    val ctx = LocalContext.current
    Column(Modifier.fillMaxSize().padding(DreamSpacing.lg)) {
        Text("First, let's bring the dream-reader home")
        if (p != null) {
            LinearProgressIndicator(
                progress = { (p.first.toFloat() / p.second.toFloat().coerceAtLeast(1f)) },
            )
        }
        Button(
            onClick = {
                vm.startDownload(ctx.applicationContext)
            },
        ) { Text("Download (2.6 GB)") }
        Button(onClick = onDone) { Text("Skip to permissions (dev)") }
    }
}
