package com.charles.app.dreamloom.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonOff
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.charles.app.dreamloom.R
import com.charles.app.dreamloom.ui.theme.CormorantFontItalic
import com.charles.app.dreamloom.ui.theme.DreamColors
import com.charles.app.dreamloom.ui.theme.DreamSpacing

@Composable
fun WelcomeScreen(onContinue: () -> Unit) {
    OnboardingScreenContainer(
        step = 1,
        totalSteps = 4,
        title = stringResource(R.string.onboard_welcome_title),
        subtitle = stringResource(R.string.onboard_welcome_lead),
        heroIcon = Icons.Outlined.AutoAwesome,
    ) {
        Column(
            Modifier
                .fillMaxWidth(),
        ) {
            Row(Modifier.fillMaxWidth()) {
                OnboardingTag(text = stringResource(R.string.onboard_welcome_tag_new))
                Spacer(Modifier.width(8.dp))
                OnboardingTag(text = stringResource(R.string.onboard_welcome_tag_private))
            }
            Spacer(Modifier.height(DreamSpacing.md))
            Image(
                painter = painterResource(R.drawable.ill_dream_hero),
                contentDescription = stringResource(R.string.cd_home_hero),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Fit,
            )
            Spacer(Modifier.height(DreamSpacing.md))
            Text(
                text = stringResource(R.string.home_tagline),
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = CormorantFontItalic),
                color = DreamColors.Ink,
            )
            Spacer(Modifier.height(DreamSpacing.md))
            Row(Modifier.fillMaxWidth()) {
                Icon(
                    imageVector = Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = DreamColors.AuroraSoft,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.onboard_welcome_support),
                    style = MaterialTheme.typography.bodyMedium,
                    color = DreamColors.InkMuted,
                )
            }
            Text(
                text = stringResource(R.string.onboard_welcome_note),
                style = MaterialTheme.typography.labelMedium,
                color = DreamColors.InkMuted,
            )
            Spacer(Modifier.height(DreamSpacing.lg))
            OnboardingPrimaryButton(
                text = stringResource(R.string.onboard_welcome_cta),
                onClick = onContinue,
            )
        }
    }
}

@Composable
fun OnboardingPrivacyScreen(onContinue: () -> Unit) {
    OnboardingScreenContainer(
        step = 2,
        totalSteps = 4,
        title = stringResource(R.string.onboard_privacy_title),
        subtitle = stringResource(R.string.onboard_privacy_lead),
        heroIcon = Icons.Outlined.Lock,
    ) {
        Column(
            Modifier
                .fillMaxWidth(),
        ) {
            Row(Modifier.fillMaxWidth()) {
                OnboardingTag(text = stringResource(R.string.onboard_privacy_tag_local))
                Spacer(Modifier.width(8.dp))
                OnboardingTag(text = stringResource(R.string.onboard_privacy_tag_export))
            }
            Spacer(Modifier.height(DreamSpacing.md))
            OnboardingFeatureCard(
                icon = Icons.Outlined.PersonOff,
                title = stringResource(R.string.onboard_privacy_no_account_title),
                body = stringResource(R.string.onboard_privacy_no_account_body),
            )
            Spacer(Modifier.height(8.dp))
            OnboardingFeatureCard(
                icon = Icons.Outlined.CloudOff,
                title = stringResource(R.string.onboard_privacy_no_cloud_title),
                body = stringResource(R.string.onboard_privacy_no_cloud_body),
            )
            Spacer(Modifier.height(8.dp))
            OnboardingFeatureCard(
                icon = Icons.Outlined.Psychology,
                title = stringResource(R.string.onboard_privacy_device_title),
                body = stringResource(R.string.onboard_privacy_device_body),
            )
            Spacer(Modifier.height(DreamSpacing.md))
            HorizontalDivider(color = DreamColors.InkFaint.copy(alpha = 0.25f))
            Spacer(Modifier.height(DreamSpacing.sm))
            Text(
                text = stringResource(R.string.onboard_privacy_footer),
                style = MaterialTheme.typography.labelMedium,
                color = DreamColors.InkMuted,
            )
            Spacer(Modifier.height(DreamSpacing.sm))
            OnboardingPrimaryButton(
                text = stringResource(R.string.onboard_privacy_cta),
                onClick = onContinue,
            )
        }
    }
}
