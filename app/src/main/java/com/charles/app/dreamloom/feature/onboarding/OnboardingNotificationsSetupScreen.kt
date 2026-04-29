package com.charles.app.dreamloom.feature.onboarding

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charles.app.dreamloom.R
import com.charles.app.dreamloom.ui.theme.DreamColors
import com.charles.app.dreamloom.ui.theme.DreamSpacing
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingNotificationsSetupScreen(
    onDone: () -> Unit,
    vm: OnboardingNotificationsSetupViewModel = hiltViewModel(),
) {
    val ctx = LocalContext.current
    val enabled by vm.reminderEnabled.collectAsStateWithLifecycle()
    val hour by vm.reminderHour.collectAsStateWithLifecycle()
    val minute by vm.reminderMinute.collectAsStateWithLifecycle()
    val reminderText by vm.reminderText.collectAsStateWithLifecycle()
    var draft by remember(reminderText) { mutableStateOf(reminderText) }
    var showTime by remember { mutableStateOf(false) }
    var showNotificationsPrompt by remember { mutableStateOf(false) }
    val is24 = DateFormat.is24HourFormat(ctx)

    fun areAppNotificationsEnabled(): Boolean = NotificationManagerCompat.from(ctx).areNotificationsEnabled()
    fun openAppNotificationSettings() {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, ctx.packageName)
        }
        ctx.startActivity(intent)
    }

    LaunchedEffect(enabled) {
        if (enabled && !areAppNotificationsEnabled()) {
            vm.setEnabled(false)
            showNotificationsPrompt = true
        }
    }

    val permAsk = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted && areAppNotificationsEnabled()) {
            vm.setEnabled(true)
            showNotificationsPrompt = false
        } else {
            showNotificationsPrompt = true
        }
    }

    OnboardingScreenContainer(
        step = 5,
        totalSteps = 5,
        title = stringResource(R.string.onboard_notifications_setup_title),
        subtitle = stringResource(R.string.onboard_notifications_setup_lead),
        heroIcon = Icons.Outlined.NotificationsActive,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(Modifier.fillMaxWidth()) {
                OnboardingTag(text = stringResource(R.string.onboard_notifications_setup_tag_optional))
            }
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(stringResource(R.string.settings_morning_nudge), style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = enabled,
                    onCheckedChange = { on ->
                        if (on) {
                            if (!areAppNotificationsEnabled()) {
                                showNotificationsPrompt = true
                                return@Switch
                            }
                            if (Build.VERSION.SDK_INT >= 33) {
                                val have = ContextCompat.checkSelfPermission(
                                    ctx,
                                    Manifest.permission.POST_NOTIFICATIONS,
                                ) == PackageManager.PERMISSION_GRANTED
                                if (have) {
                                    vm.setEnabled(true)
                                    showNotificationsPrompt = false
                                } else {
                                    permAsk.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else {
                                vm.setEnabled(true)
                                showNotificationsPrompt = false
                            }
                        } else {
                            vm.setEnabled(false)
                            showNotificationsPrompt = false
                        }
                    },
                )
            }
            if (showNotificationsPrompt) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = DreamColors.IndigoSoft.copy(alpha = 0.45f),
                    ),
                ) {
                    Column(
                        Modifier.padding(DreamSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.settings_notifications_disabled_title),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = stringResource(R.string.settings_notifications_disabled_body),
                            style = MaterialTheme.typography.bodyMedium,
                            color = DreamColors.InkMuted,
                        )
                        Button(onClick = { openAppNotificationSettings() }) {
                            Text(stringResource(R.string.settings_open_notification_settings))
                        }
                    }
                }
            }
            if (showTime) {
                val pickerState = rememberTimePickerState(
                    initialHour = hour,
                    initialMinute = minute,
                    is24Hour = is24,
                )
                AlertDialog(
                    onDismissRequest = { showTime = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                vm.setTime(pickerState.hour, pickerState.minute)
                                showTime = false
                            },
                        ) { Text(stringResource(R.string.ok)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showTime = false }) { Text(stringResource(R.string.back)) }
                    },
                    text = { TimePicker(pickerState) },
                )
            }
            Button(
                onClick = { showTime = true },
                enabled = enabled,
            ) {
                Text(stringResource(R.string.settings_reminder_time, formatOnboardingTime(hour, minute, is24)))
            }
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                label = { Text(stringResource(R.string.settings_what_to_say)) },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = { vm.setReminderText(draft) }) {
                Text(stringResource(R.string.save_reminder_text))
            }
            Spacer(Modifier.height(8.dp))
            OnboardingPrimaryButton(
                text = stringResource(R.string.onboard_notifications_setup_cta),
                onClick = {
                    vm.completeOnboarding()
                    onDone()
                },
            )
        }
    }
}

private fun formatOnboardingTime(hour: Int, minute: Int, is24: Boolean): String {
    if (is24) {
        return String.format(Locale.getDefault(), "%d:%02d", hour, minute)
    }
    val am = hour < 12
    var hour12 = hour % 12
    if (hour12 == 0) hour12 = 12
    val ampm = if (am) "a.m." else "p.m."
    return "$hour12:${String.format("%02d", minute)} $ampm"
}
