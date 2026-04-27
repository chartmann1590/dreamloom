package com.charles.app.dreamloom.feature.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.charles.app.dreamloom.ui.theme.DreamSpacing

@Composable
fun OnboardingPermissionsScreen(
    onDone: () -> Unit,
    vm: OnboardingPermissionsViewModel = hiltViewModel(),
) {
    val perms = buildList {
        add(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        vm.markOnboardingComplete()
        onDone()
    }
    Column(Modifier.fillMaxSize().padding(DreamSpacing.lg)) {
        Text("So you can whisper your dream while your eyes are still closed")
        Text("Optional — a gentle morning nudge")
        Button(
            onClick = { launcher.launch(perms.toTypedArray()) },
        ) { Text("Continue") }
    }
}
