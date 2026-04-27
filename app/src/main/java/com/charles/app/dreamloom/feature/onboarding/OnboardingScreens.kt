package com.charles.app.dreamloom.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.charles.app.dreamloom.ui.theme.DreamSpacing

@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(DreamSpacing.lg), verticalArrangement = Arrangement.Center) {
        Text("Dreamloom", style = androidx.compose.material3.MaterialTheme.typography.displaySmall)
        Text("Your dreams, decoded. Privately.")
        Button(onClick = onContinue) { Text("Begin") }
    }
}

@Composable
fun OnboardingPrivacyScreen(onContinue: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(DreamSpacing.lg)) {
        Text("No account")
        Text("No cloud")
        Text("Your dreams stay on your phone")
        Button(onClick = onContinue) { Text("Continue") }
    }
}
