package com.charles.app.dreamloom.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.charles.app.dreamloom.data.prefs.ThemeMode
import com.charles.app.dreamloom.ui.nav.AppNavHost
import com.charles.app.dreamloom.ui.theme.DreamColors
import com.charles.app.dreamloom.ui.theme.DreamloomTheme

@Composable
fun DreamloomApp() {
    val shellVm: ShellViewModel = hiltViewModel()
    val theme by shellVm.themeMode.collectAsState(initial = ThemeMode.Dark)
    DreamloomTheme(themeMode = theme) {
        Surface(Modifier.fillMaxSize(), color = DreamColors.Night) {
            val nav = rememberNavController()
            AppNavHost(navController = nav)
        }
    }
}
