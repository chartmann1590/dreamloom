package com.charles.app.dreamloom.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.charles.app.dreamloom.navigation.Routes
import com.charles.app.dreamloom.ui.theme.DreamColors
import com.charles.app.dreamloom.ui.theme.DreamSpacing
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onDone: (String) -> Unit,
    vm: SplashViewModel = hiltViewModel(),
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DreamColors.Night),
        contentAlignment = Alignment.Center,
    ) {
        Text("Dreamloom", color = DreamColors.Moonglow, modifier = Modifier, style = androidx.compose.material3.MaterialTheme.typography.displaySmall)
    }
    LaunchedEffect(Unit) {
        delay(600)
        onDone(vm.nextDestination())
    }
}
