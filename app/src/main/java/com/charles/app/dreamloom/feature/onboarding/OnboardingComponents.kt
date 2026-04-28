package com.charles.app.dreamloom.feature.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.charles.app.dreamloom.ui.components.AuroraStarfieldBackground
import com.charles.app.dreamloom.ui.theme.DreamColors
import com.charles.app.dreamloom.ui.theme.DreamSpacing

@Composable
fun OnboardingScreenContainer(
    step: Int,
    totalSteps: Int,
    title: String,
    subtitle: String,
    heroIcon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    AuroraStarfieldBackground(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = DreamSpacing.lg, vertical = DreamSpacing.lg),
        ) {
            OnboardingProgress(step = step, totalSteps = totalSteps)
            Spacer(Modifier.height(DreamSpacing.lg))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DreamSpacing.md),
                modifier = Modifier.fillMaxWidth(),
            ) {
                OnboardingHeroBadge(icon = heroIcon)
                Column(Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = DreamColors.Moonglow,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = DreamColors.InkMuted,
                    )
                }
            }
            Spacer(Modifier.height(DreamSpacing.lg))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DreamColors.IndigoSoft.copy(alpha = 0.48f),
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = DreamColors.InkFaint.copy(alpha = 0.35f),
                ),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DreamSpacing.md, vertical = DreamSpacing.lg),
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun OnboardingProgress(step: Int, totalSteps: Int) {
    Column(Modifier.fillMaxWidth()) {
        OnboardingTag(text = "STEP $step OF $totalSteps")
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(totalSteps) { idx ->
                val selected = idx < step
                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .weight(1f)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (selected) DreamColors.AuroraSoft
                            else DreamColors.Indigo.copy(alpha = 0.6f),
                        ),
                )
            }
        }
    }
}

@Composable
private fun OnboardingHeroBadge(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(66.dp)
            .clip(CircleShape)
            .background(DreamColors.Indigo.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = DreamColors.AuroraSoft,
            modifier = Modifier.size(34.dp),
        )
    }
}

@Composable
fun OnboardingTag(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(DreamColors.Indigo.copy(alpha = 0.55f))
            .border(
                width = 1.dp,
                color = DreamColors.InkFaint.copy(alpha = 0.35f),
                shape = RoundedCornerShape(999.dp),
            )
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = DreamColors.InkMuted,
        )
    }
}

@Composable
fun OnboardingPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = DreamColors.Aurora,
            contentColor = DreamColors.Ink,
            disabledContainerColor = DreamColors.IndigoSoft,
            disabledContentColor = DreamColors.InkMuted,
        ),
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Outlined.ArrowForward,
            contentDescription = null,
            tint = DreamColors.Ink,
        )
    }
}

@Composable
fun OnboardingFeatureCard(
    icon: ImageVector,
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0x332B1D66),
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, DreamColors.InkFaint.copy(alpha = 0.22f)),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(DreamSpacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = DreamColors.Moonglow,
            )
            Spacer(Modifier.width(DreamSpacing.md))
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = DreamColors.MoonglowSoft,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DreamColors.Ink,
                    textAlign = TextAlign.Start,
                )
            }
        }
    }
}
