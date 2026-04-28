package com.charles.app.dreamloom

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.core.view.WindowCompat
import com.charles.app.dreamloom.ui.DreamloomApp
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val pendingOpenRoute = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        readOpenRoute(intent)
        setContent {
            DreamloomApp(
                pendingOpenRoute = pendingOpenRoute,
                onConsumedOpenRoute = { pendingOpenRoute.value = null },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readOpenRoute(intent)
    }

    private fun readOpenRoute(i: Intent?) {
        pendingOpenRoute.value = i?.getStringExtra(EXTRA_OPEN_ROUTE)
    }

    companion object {
        const val EXTRA_OPEN_ROUTE: String = "com.charles.app.dreamloom.OPEN_ROUTE"
    }
}
