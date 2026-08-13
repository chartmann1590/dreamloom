package com.charles.app.dreamloom.feature.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.format.DateFormat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.charles.app.dreamloom.BuildConfig
import com.charles.app.dreamloom.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charles.app.dreamloom.data.prefs.ThemeMode
import com.charles.app.dreamloom.llm.ModelConfig
import com.charles.app.dreamloom.ads.NativeAdCard
import com.charles.app.dreamloom.ui.components.AuroraStarfieldBackground
import com.charles.app.dreamloom.ui.theme.DreamColors
import com.charles.app.dreamloom.ui.theme.DreamSpacing
import java.util.Locale

@Composable
private fun SettingsLinkCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = DreamColors.IndigoSoft.copy(alpha = 0.45f),
        ),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = DreamSpacing.md, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DreamColors.Moonglow,
                modifier = Modifier.size(26.dp),
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = DreamColors.Ink,
                modifier = Modifier.weight(1f),
            )
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = DreamColors.InkMuted,
            )
        }
    }
}

@Composable
fun SettingsRootScreen(
    onPrivacy: () -> Unit,
    onReminders: () -> Unit,
    onAbout: () -> Unit,
    onMoreApps: () -> Unit = {},
    vm: SettingsRootViewModel = hiltViewModel(),
) {
    val ctx = LocalContext.current
    val theme by vm.themeMode.collectAsStateWithLifecycle()
    val autoStop by vm.autoStopAfterSilence.collectAsStateWithLifecycle()
    AuroraStarfieldBackground(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = DreamSpacing.md, vertical = DreamSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(DreamSpacing.sm),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DreamSpacing.sm),
            ) {
                Icon(
                    Icons.Outlined.DarkMode,
                    contentDescription = null,
                    tint = DreamColors.Moonglow,
                    modifier = Modifier.size(36.dp),
                )
                Column {
                    Text(
                        stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.headlineSmall,
                        color = DreamColors.Moonglow,
                    )
                    Text(
                        stringResource(R.string.settings_header_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = DreamColors.InkFaint,
                    )
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DreamColors.IndigoSoft.copy(alpha = 0.35f),
                ),
            ) {
                Column(
                    Modifier.padding(DreamSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(DreamSpacing.sm),
                ) {
                    Text(
                        stringResource(R.string.settings_theme_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = DreamColors.InkMuted,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        FilterChip(
                            selected = theme == ThemeMode.Dark,
                            onClick = { vm.setTheme(ThemeMode.Dark) },
                            label = { Text(stringResource(R.string.settings_theme_dark)) },
                        )
                        FilterChip(
                            selected = theme == ThemeMode.Light,
                            onClick = { vm.setTheme(ThemeMode.Light) },
                            label = { Text(stringResource(R.string.settings_theme_light)) },
                        )
                        FilterChip(
                            selected = theme == ThemeMode.System,
                            onClick = { vm.setTheme(ThemeMode.System) },
                            label = { Text(stringResource(R.string.settings_theme_system)) },
                        )
                    }
                    HorizontalDivider(color = DreamColors.InkFaint.copy(alpha = 0.2f))
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            stringResource(R.string.settings_voice_auto_stop),
                            style = MaterialTheme.typography.bodyLarge,
                            color = DreamColors.Ink,
                        )
                        Switch(
                            checked = autoStop,
                            onCheckedChange = { vm.setAutoStopAfterSilence(it) },
                        )
                    }
                    Text(
                        stringResource(R.string.settings_voice_auto_stop_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = DreamColors.InkMuted,
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            SettingsLinkCard(
                icon = Icons.Outlined.Shield,
                label = stringResource(R.string.settings_privacy_nav),
                onClick = onPrivacy,
            )
            SettingsLinkCard(
                icon = Icons.Outlined.NotificationsActive,
                label = stringResource(R.string.settings_reminders_nav),
                onClick = onReminders,
            )
            SettingsLinkCard(
                icon = Icons.Outlined.Info,
                label = stringResource(R.string.settings_about_nav),
                onClick = onAbout,
            )
            SettingsLinkCard(
                icon = Icons.Outlined.Apps,
                label = "More apps from this developer",
                onClick = onMoreApps,
            )
            SettingsLinkCard(
                icon = Icons.Outlined.MailOutline,
                label = stringResource(R.string.settings_send_feedback),
                onClick = {
                    ctx.startActivity(
                        Intent(Intent.ACTION_SENDTO).setData(Uri.parse("mailto:hello@dreamloom.app")),
                    )
                },
            )
            Spacer(Modifier.height(DreamSpacing.sm))
            SupportFeedbackSection(viewModel = hiltViewModel())
        }
    }
}

@Composable
fun PrivacySettingsScreen(
    vm: PrivacySettingsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val wiping by vm.wiping.collectAsStateWithLifecycle()
    val err by vm.message.collectAsStateWithLifecycle()
    val analyticsOn by vm.analyticsOptIn.collectAsStateWithLifecycle()
    val crashOn by vm.crashlyticsOptIn.collectAsStateWithLifecycle()
    var showWipeFirst by remember { mutableStateOf(false) }
    var showWipeSecond by remember { mutableStateOf(false) }
    AuroraStarfieldBackground(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = DreamSpacing.md, vertical = DreamSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
        Text(
            stringResource(R.string.settings_privacy_nav),
            style = MaterialTheme.typography.headlineSmall,
            color = DreamColors.Moonglow,
        )
        Text(stringResource(R.string.settings_privacy_receipt_data), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.settings_privacy_receipt_account), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.settings_privacy_receipt_cloud), style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(4.dp))
        Text(stringResource(R.string.settings_privacy_collect_title), style = MaterialTheme.typography.titleSmall)
        Text(stringResource(R.string.settings_privacy_collect_journal), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.settings_privacy_collect_usage), style = MaterialTheme.typography.bodyMedium)
        Text(stringResource(R.string.settings_privacy_collect_crash_line), style = MaterialTheme.typography.bodyMedium)
        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = DreamColors.InkFaint.copy(alpha = 0.25f))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.settings_analytics_toggle), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(end = 8.dp))
            Switch(checked = analyticsOn, onCheckedChange = { vm.setAnalyticsOptIn(it) }, enabled = !wiping)
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.settings_crash_toggle), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f).padding(end = 8.dp))
            Switch(checked = crashOn, onCheckedChange = { vm.setCrashlyticsOptIn(it) }, enabled = !wiping)
        }
        HorizontalDivider(Modifier.padding(vertical = 8.dp), color = DreamColors.InkFaint.copy(alpha = 0.25f))
        Text(stringResource(R.string.settings_wipe_intro), style = MaterialTheme.typography.bodyMedium)
        if (err != null) {
            Text(err!!, color = MaterialTheme.colorScheme.error)
        }
        Button(
            onClick = { showWipeFirst = true },
            enabled = !wiping,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.onError,
            ),
        ) {
            if (wiping) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onError)
                    Text(stringResource(R.string.settings_wiping))
                }
            } else {
                Text(stringResource(R.string.settings_wipe_everything))
            }
        }
        TextButton(onClick = onBack, enabled = !wiping) { Text(stringResource(R.string.back)) }
        }
    }
    if (showWipeFirst) {
        AlertDialog(
            onDismissRequest = { if (!wiping) showWipeFirst = false },
            title = { Text(stringResource(R.string.settings_wipe_confirm_title)) },
            text = { Text(stringResource(R.string.settings_wipe_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showWipeFirst = false
                        showWipeSecond = true
                    },
                    enabled = !wiping,
                ) { Text(stringResource(R.string.settings_wipe_continue)) }
            },
            dismissButton = {
                TextButton(onClick = { showWipeFirst = false }, enabled = !wiping) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
    if (showWipeSecond) {
        AlertDialog(
            onDismissRequest = { if (!wiping) showWipeSecond = false },
            title = { Text(stringResource(R.string.settings_wipe_final_title)) },
            text = { Text(stringResource(R.string.settings_wipe_final_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showWipeSecond = false
                        vm.wipeAll()
                    },
                    enabled = !wiping,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text(stringResource(R.string.settings_wipe_erase)) }
            },
            dismissButton = {
                TextButton(onClick = { showWipeSecond = false }, enabled = !wiping) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen(
    vm: RemindersViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val ctx = LocalContext.current
    val enabled by vm.reminderEnabled.collectAsStateWithLifecycle()
    val h by vm.reminderHour.collectAsStateWithLifecycle()
    val m by vm.reminderMinute.collectAsStateWithLifecycle()
    val line by vm.reminderText.collectAsStateWithLifecycle()
    var draft by remember(line) { mutableStateOf(line) }
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
    val permAsk = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (ok && areAppNotificationsEnabled()) {
            vm.setEnabled(true)
            showNotificationsPrompt = false
        } else {
            showNotificationsPrompt = true
        }
    }
    AuroraStarfieldBackground(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = DreamSpacing.md, vertical = DreamSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
        Text(
            stringResource(R.string.settings_reminders_title),
            style = MaterialTheme.typography.headlineSmall,
            color = DreamColors.Moonglow,
        )
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
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
                                android.Manifest.permission.POST_NOTIFICATIONS,
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            if (have) {
                                vm.setEnabled(true)
                                showNotificationsPrompt = false
                            }
                            else permAsk.launch(android.Manifest.permission.POST_NOTIFICATIONS)
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
                shape = RoundedCornerShape(12.dp),
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
            val pState = rememberTimePickerState(initialHour = h, initialMinute = m, is24Hour = is24)
            AlertDialog(
                onDismissRequest = { showTime = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            vm.setTime(pState.hour, pState.minute)
                            showTime = false
                        },
                    ) { Text(stringResource(R.string.ok)) }
                },
                dismissButton = { TextButton(onClick = { showTime = false }) { Text(stringResource(R.string.back)) } },
                text = { TimePicker(pState) },
            )
        }
        Button(
            onClick = { showTime = true },
            enabled = enabled,
        ) { Text(stringResource(R.string.settings_reminder_time, formatTime(h, m, is24))) }
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            label = { Text(stringResource(R.string.settings_what_to_say)) },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = { vm.setReminderText(draft) },
        ) { Text(stringResource(R.string.save_reminder_text)) }
        Button(onClick = onBack) { Text(stringResource(R.string.back)) }
        }
    }
}

private fun formatTime(h: Int, min: Int, is24: Boolean): String {
    if (is24) {
        return String.format(Locale.getDefault(), "%d:%02d", h, min)
    }
    val am = h < 12
    var hour12 = h % 12
    if (hour12 == 0) hour12 = 12
    val ampm = if (am) "a.m." else "p.m."
    return "$hour12:${String.format("%02d", min)} $ampm"
}

@Composable
fun AboutScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val privacyUrl = stringResource(R.string.privacy_policy_url)
    AuroraStarfieldBackground(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = DreamSpacing.md, vertical = DreamSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(DreamSpacing.md),
        ) {
        Text(
            stringResource(R.string.settings_about_nav),
            style = MaterialTheme.typography.headlineSmall,
            color = DreamColors.Moonglow,
        )
        Text(
            stringResource(R.string.settings_about_version_line, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE),
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            stringResource(R.string.settings_about_model_line, ModelConfig.ABOUT_LABEL),
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(stringResource(R.string.settings_about_ads_body), style = MaterialTheme.typography.bodyMedium)
        NativeAdCard(modifier = Modifier.fillMaxWidth())
        TextButton(
            onClick = {
                ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(privacyUrl)))
            },
        ) {
            Text(stringResource(R.string.settings_about_privacy_link))
        }
        Text(
            stringResource(R.string.settings_about_credit),
            style = MaterialTheme.typography.bodySmall,
            color = DreamColors.InkMuted,
        )
        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                val i = Intent(ctx, com.google.android.gms.oss.licenses.OssLicensesMenuActivity::class.java)
                ctx.startActivity(i)
            },
        ) {
            Text(stringResource(R.string.settings_about_licenses))
        }
        TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
        }
    }
}

private data class CrossPromoApp(val name: String, val packageName: String, val tagline: String)

private val crossPromoApps: List<CrossPromoApp> = listOf(
    CrossPromoApp("NutriSnap: AI Calorie Tracker", "com.charles.nutrisnap", "Snap a meal, get instant calories & macros — 100% on-device AI, private."),
    CrossPromoApp("Aria: On-Device Assistant", "com.aria.assistant", "Private on-device voice AI with optional, source-backed web verification."),
    CrossPromoApp("ScamRadar: AI Scam Detector", "com.charles.scamradar.app", "On-device AI catches scams in texts, voicemails & notifications. Free."),
    CrossPromoApp("MeshTalk: Bluetooth Mesh Chat", "com.charles.meshtalk.app", "Chat, talk & AI over Bluetooth mesh. No internet, no accounts, fully offline."),
    CrossPromoApp("DriveVault Dashcam", "com.drivevault.dashcam", "Privacy-first dashcam: GPS overlays, dual-camera, background recording."),
    CrossPromoApp("PixelDream: Offline AI Images", "com.hartmann.pixeldream", "Private, offline AI image generator. Your prompts and pictures never leave."),
    CrossPromoApp("Pocket-Assistant", "com.charles.pocketassistant", "Local AI organizer: save bills & notes, chat, tasks, and reminders on-device."),
    CrossPromoApp("TextPilot AI Messaging", "com.charles.messenger.v2", "Clean, fast SMS app with AI smart replies and web browser access to your texts."),
    CrossPromoApp("Pixel Fish Tank", "com.charles.virtualpet.fishtank", "Cozy virtual pet game — feed, clean & customize your pixel fish. Play & relax!"),
    CrossPromoApp("TrailSage AI: Road Trip Guide", "com.charles.trailsage", "Private, offline GPS audio tour guide with on-device AI storytelling."),
    CrossPromoApp("Knightfall: Chess with AI Coach", "com.chartmann.knightfall", "Play chess against Stockfish AI with Gemma 4 coaching, online, or on the web!"),
    CrossPromoApp("CaptionBurn: Video Captions", "com.charlesh.captionburn", "On-device auto-captions, burned into your video, with built-in translation."),
    CrossPromoApp("Jury Simulator: Trial Verdict", "com.charles.jurysim", "Step into jury duty with AI trials, eleven jurors, and the verdict in your hands."),
    CrossPromoApp("Photobooth Event Camera", "com.charles.photobooth", "Turn any Android device into a fun event photo booth with sharing and prints."),
    CrossPromoApp("Path - Daily Bible Study", "com.biblereadingpath.app", "Build daily Bible study habits with gentle streaks."),
    CrossPromoApp("SkyPulse: Live Flight Tracker", "com.charles.skypulse.app", "Track live flights overhead in real time — aircraft, airports & smart alerts."),
    CrossPromoApp("Grocy Fridge Scanner", "com.charleshartmann.grocyfridge", "Snap your fridge. On-device AI updates your Grocy stock in seconds. No cloud."),
    CrossPromoApp("CrowdTransit: Bus & Train", "com.charles.crowdtransit.app", "Find your ride — free live transit stops, schedules & reviews, nationwide."),
)

@Composable
fun MoreAppsScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    AuroraStarfieldBackground(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = DreamSpacing.md, vertical = DreamSpacing.lg),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DreamSpacing.sm),
            ) {
                Icon(
                    Icons.Outlined.Apps,
                    contentDescription = null,
                    tint = DreamColors.Moonglow,
                    modifier = Modifier.size(32.dp),
                )
                Text(
                    "More from this developer",
                    style = MaterialTheme.typography.headlineSmall,
                    color = DreamColors.Moonglow,
                )
            }
            Spacer(Modifier.height(DreamSpacing.md))
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(DreamSpacing.xs),
            ) {
                crossPromoApps.forEach { app ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                ctx.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${app.packageName}"))
                                )
                            },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = DreamColors.IndigoSoft.copy(alpha = 0.45f),
                        ),
                    ) {
                        Column(Modifier.padding(horizontal = DreamSpacing.md, vertical = 14.dp)) {
                            Text(app.name, style = MaterialTheme.typography.bodyLarge, color = DreamColors.Ink)
                            Text(app.tagline, style = MaterialTheme.typography.bodySmall, color = DreamColors.InkMuted)
                        }
                    }
                }
                Spacer(Modifier.height(DreamSpacing.md))
                TextButton(onClick = onBack) { Text(stringResource(R.string.back)) }
            }
        }
    }
}
