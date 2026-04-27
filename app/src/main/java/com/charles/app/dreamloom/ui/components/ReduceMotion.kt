package com.charles.app.dreamloom.ui.components

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun rememberPrefersReducedMotion(): Boolean {
    val c = LocalContext.current
    return remember(c) {
        runCatching {
            Settings.Global.getFloat(c.contentResolver, Settings.Global.TRANSITION_ANIMATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
    }
}
