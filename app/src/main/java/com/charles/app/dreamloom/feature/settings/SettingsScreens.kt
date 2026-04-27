package com.charles.app.dreamloom.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun SettingsRootScreen(
    onPrivacy: () -> Unit,
    onReminders: () -> Unit,
    onAbout: () -> Unit,
) {
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = onPrivacy) { Text("Privacy") }
        Button(onClick = onReminders) { Text("Reminders") }
        Button(onClick = onAbout) { Text("About") }
    }
}

@Composable
fun PrivacySettingsScreen(
    vm: PrivacySettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val wiping by vm.wiping.collectAsStateWithLifecycle()
    val err by vm.message.collectAsStateWithLifecycle()
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Privacy", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Wipe all local data: dreams, model file, and settings. " +
                "The app will restart. This cannot be undone.",
            style = MaterialTheme.typography.bodyMedium,
        )
        if (err != null) {
            Text(err!!, color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = { vm.wipeAll() },
            enabled = !wiping,
        ) {
            if (wiping) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Text("Wiping…")
                }
            } else {
                Text("Wipe all local data")
            }
        }
        Button(onClick = onBack, enabled = !wiping) { Text("Back") }
    }
}

@Composable
fun RemindersScreen(onBack: () -> Unit) {
    Text("Reminders")
    Button(onClick = onBack) { Text("Back") }
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    Column {
        Text("Version")
        Button(onClick = { val i = Intent(ctx, com.google.android.gms.oss.licenses.OssLicensesMenuActivity::class.java); ctx.startActivity(i) }) {
            Text("Open source licenses")
        }
        Button(
            onClick = {
                ctx.startActivity(
                    Intent(Intent.ACTION_SENDTO).setData(Uri.parse("mailto:hello@dreamloom.app")),
                )
            },
        ) { Text("Send feedback") }
        Button(onClick = onBack) { Text("Back") }
    }
}
