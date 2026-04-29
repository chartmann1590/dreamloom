package com.charles.app.dreamloom

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import androidx.core.view.WindowCompat
import com.charles.app.dreamloom.ads.AppOpenAdManager
import com.charles.app.dreamloom.data.prefs.AppPreferences
import com.charles.app.dreamloom.ui.DreamloomApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var appOpenAdManager: AppOpenAdManager

    @Inject
    lateinit var appPreferences: AppPreferences

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
        appOpenAdManager.preload()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        readOpenRoute(intent)
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            appOpenAdManager.showIfEligible(
                activity = this@MainActivity,
                openedFromNotification = pendingOpenRoute.value != null,
            )
            if (!appPreferences.firstInstallSessionDone.first()) {
                appPreferences.setFirstInstallSessionDone()
            }
        }
    }

    private fun readOpenRoute(i: Intent?) {
        pendingOpenRoute.value = i?.getStringExtra(EXTRA_OPEN_ROUTE)
    }

    companion object {
        const val EXTRA_OPEN_ROUTE: String = "com.charles.app.dreamloom.OPEN_ROUTE"
    }
}
