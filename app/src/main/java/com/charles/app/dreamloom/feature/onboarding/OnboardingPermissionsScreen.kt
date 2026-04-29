package com.charles.app.dreamloom.feature.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.ToggleOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.charles.app.dreamloom.R
import com.charles.app.dreamloom.ui.theme.DreamColors
import com.charles.app.dreamloom.ui.theme.DreamSpacing

@Composable
fun OnboardingPermissionsScreen(
    onDone: () -> Unit,
) {
    val perms = buildList {
        add(Manifest.permission.RECORD_AUDIO)
        if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
    }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) {
        onDone()
    }
    OnboardingScreenContainer(
        step = 4,
        totalSteps = 5,
        title = stringResource(R.string.onboard_permissions_title),
        subtitle = stringResource(R.string.onboard_permissions_lead),
        heroIcon = Icons.Outlined.ToggleOn,
    ) {
        Column(
            Modifier
                .fillMaxWidth(),
        ) {
            Row(Modifier.fillMaxWidth()) {
                OnboardingTag(text = stringResource(R.string.onboard_permissions_tag_optional))
                Spacer(Modifier.width(8.dp))
                OnboardingTag(text = stringResource(R.string.onboard_permissions_tag_change_anytime))
            }
            Spacer(Modifier.height(DreamSpacing.md))
            OnboardingFeatureCard(
                icon = Icons.Outlined.Mic,
                title = stringResource(R.string.onboard_permissions_mic_title),
                body = stringResource(R.string.onboard_permissions_mic_body),
            )
            Spacer(Modifier.height(DreamSpacing.sm))
            OnboardingFeatureCard(
                icon = Icons.Outlined.NotificationsActive,
                title = stringResource(R.string.onboard_permissions_notify_title),
                body = stringResource(R.string.onboard_permissions_notify_body),
            )
            if (Build.VERSION.SDK_INT < 33) {
                Text(
                    text = stringResource(R.string.onboard_permissions_legacy_note),
                    style = MaterialTheme.typography.labelSmall,
                    color = DreamColors.InkFaint,
                )
            }
            Spacer(Modifier.height(DreamSpacing.sm))
            Row {
                Icon(
                    Icons.Outlined.CheckCircleOutline,
                    contentDescription = null,
                    tint = DreamColors.AuroraSoft,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.onboard_permissions_footer),
                    style = MaterialTheme.typography.labelMedium,
                    color = DreamColors.InkMuted,
                )
            }
            Spacer(Modifier.height(DreamSpacing.sm))
            OnboardingPrimaryButton(
                text = stringResource(R.string.onboard_permissions_cta),
                onClick = { launcher.launch(perms.toTypedArray()) },
            )
        }
    }
}
