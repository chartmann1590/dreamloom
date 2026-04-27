package com.charles.app.dreamloom.feature.atlas

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun AtlasScreen(
    onOpenDream: (Long) -> Unit,
    vm: AtlasViewModel = hiltViewModel(),
) {
    val dreams by vm.dreams.collectAsState(initial = emptyList())
    Column {
        if (dreams.isEmpty()) {
            Text("Your Atlas is empty. Tap the moon to add your first dream.")
        } else {
            dreams.forEach { d ->
                Text(
                    text = "${d.id} — ${d.title ?: "…"}",
                    modifier = Modifier.clickable { onOpenDream(d.id) },
                )
            }
        }
    }
}
