package com.charles.app.dreamloom.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Battery6Bar
import androidx.compose.material.icons.outlined.Downloading
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.charles.app.dreamloom.BuildConfig
import com.charles.app.dreamloom.R
import com.charles.app.dreamloom.ui.theme.DreamColors
import com.charles.app.dreamloom.ui.theme.DreamSpacing

@Composable
fun ModelDownloadScreen(
    onDone: () -> Unit,
    vm: ModelDownloadViewModel = hiltViewModel(),
) {
    val ui by vm.state.collectAsState()

    LaunchedEffect(ui.phase) {
        if (ui.phase == ModelDownloadViewModel.Phase.Succeeded) onDone()
    }

    OnboardingScreenContainer(
        step = 3,
        totalSteps = 4,
        title = stringResource(R.string.onboard_model_title),
        subtitle = stringResource(R.string.onboard_model_lead),
        heroIcon = Icons.Outlined.Downloading,
    ) {
        Column(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OnboardingTag(text = stringResource(R.string.onboard_model_tag_offline))
                OnboardingTag(text = stringResource(R.string.onboard_model_size_line))
            }
            Spacer(Modifier.height(DreamSpacing.md))
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(DreamColors.IndigoSoft.copy(alpha = 0.65f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = DreamColors.AuroraSoft,
                )
            }
            Text(
                text = stringResource(R.string.onboard_model_title),
                style = MaterialTheme.typography.headlineMedium,
                color = DreamColors.Moonglow,
            )
            Text(
                text = stringResource(R.string.onboard_model_lead),
                style = MaterialTheme.typography.bodyLarge,
                color = DreamColors.Ink,
            )
            Text(
                text = stringResource(R.string.onboard_model_size_line),
                style = MaterialTheme.typography.labelLarge,
                color = DreamColors.InkMuted,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.Wifi, contentDescription = null, tint = DreamColors.InkFaint)
                    Text(
                        text = stringResource(R.string.onboard_model_hint_wifi),
                        style = MaterialTheme.typography.labelSmall,
                        color = DreamColors.InkFaint,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Outlined.Battery6Bar, contentDescription = null, tint = DreamColors.InkFaint)
                    Text(
                        text = stringResource(R.string.onboard_model_hint_power),
                        style = MaterialTheme.typography.labelSmall,
                        color = DreamColors.InkFaint,
                    )
                }
            }
            Spacer(Modifier.height(DreamSpacing.md))
            DownloadStatus(ui)
            Spacer(Modifier.height(DreamSpacing.sm))
            DownloadCta(ui = ui, onStart = vm::startDownload, onRetry = vm::retry)
            if (BuildConfig.DEBUG) {
                TextButton(
                    onClick = onDone,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        stringResource(R.string.onboard_model_skip_debug),
                        color = DreamColors.InkMuted,
                    )
                }
            }
        }
    }
}

@Composable
private fun DownloadStatus(ui: ModelDownloadViewModel.UiState) {
    when (ui.phase) {
        ModelDownloadViewModel.Phase.Idle, ModelDownloadViewModel.Phase.Succeeded -> Unit
        ModelDownloadViewModel.Phase.Queued, ModelDownloadViewModel.Phase.Blocked -> {
            Text(
                text = stringResource(R.string.onboard_model_downloading),
                style = MaterialTheme.typography.labelMedium,
                color = DreamColors.InkFaint,
            )
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = DreamColors.AuroraSoft,
                trackColor = DreamColors.IndigoSoft,
            )
        }
        ModelDownloadViewModel.Phase.Running -> {
            if (ui.total > 0L) {
                val frac = (ui.downloaded.toFloat() / ui.total.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { frac },
                    modifier = Modifier.fillMaxWidth(),
                    color = DreamColors.AuroraSoft,
                    trackColor = DreamColors.IndigoSoft,
                )
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth()) {
                    Text(
                        text = "${(frac * 100).toInt()}% (${ui.downloaded / 1_000_000} / ${ui.total / 1_000_000} MB)",
                        style = MaterialTheme.typography.labelSmall,
                        color = DreamColors.InkFaint,
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.onboard_model_downloading),
                    style = MaterialTheme.typography.labelMedium,
                    color = DreamColors.InkFaint,
                )
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = DreamColors.AuroraSoft,
                    trackColor = DreamColors.IndigoSoft,
                )
            }
        }
        ModelDownloadViewModel.Phase.Failed -> {
            Text(
                text = "Download failed. Check your connection and try again.",
                style = MaterialTheme.typography.labelMedium,
                color = DreamColors.Moonglow,
            )
        }
    }
}

@Composable
private fun DownloadCta(
    ui: ModelDownloadViewModel.UiState,
    onStart: () -> Unit,
    onRetry: () -> Unit,
) {
    when (ui.phase) {
        ModelDownloadViewModel.Phase.Idle ->
            OnboardingPrimaryButton(
                text = stringResource(R.string.onboard_model_download_cta),
                onClick = onStart,
            )
        ModelDownloadViewModel.Phase.Failed ->
            OnboardingPrimaryButton(text = "Try again", onClick = onRetry)
        ModelDownloadViewModel.Phase.Queued,
        ModelDownloadViewModel.Phase.Running,
        ModelDownloadViewModel.Phase.Blocked,
        ModelDownloadViewModel.Phase.Succeeded ->
            OnboardingPrimaryButton(
                text = stringResource(R.string.onboard_model_download_cta),
                onClick = {},
                enabled = false,
            )
    }
}
