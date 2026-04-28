package com.charles.app.dreamloom.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.charles.app.dreamloom.R
import com.charles.app.dreamloom.core.lunar.lastNightMoonPhase
import com.charles.app.dreamloom.ui.components.AuroraStarfieldBackground
import com.charles.app.dreamloom.ui.components.MoonButton
import com.charles.app.dreamloom.ui.components.MoonPhaseLine
import com.charles.app.dreamloom.ui.components.SectionLabel
import com.charles.app.dreamloom.ui.theme.CormorantFontItalic
import com.charles.app.dreamloom.ui.theme.DreamColors
import com.charles.app.dreamloom.ui.theme.DreamSpacing

@Composable
fun HomeScreen(
    onNewDream: () -> Unit,
    onOracle: () -> Unit,
) {
    val phase = lastNightMoonPhase()

    AuroraStarfieldBackground(Modifier.fillMaxSize()) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val raw = minOf(maxWidth, maxHeight) * 0.42f
            val moonSize = minOf(300.dp, maxOf(200.dp, raw))
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = DreamSpacing.xl)
                    .padding(top = DreamSpacing.md),
            ) {
                Image(
                    painter = painterResource(R.drawable.ill_dream_hero),
                    contentDescription = stringResource(R.string.cd_home_hero),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentScale = ContentScale.Fit,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineLarge,
                    color = DreamColors.Moonglow,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = stringResource(R.string.home_tagline),
                    style = MaterialTheme.typography.bodyMedium.copy(fontFamily = CormorantFontItalic),
                    color = DreamColors.InkMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, start = DreamSpacing.sm, end = DreamSpacing.sm),
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.weight(1f))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    MoonButton(
                        onClick = onNewDream,
                        size = moonSize,
                        contentDescription = stringResource(R.string.cd_record_dream),
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.home_tap_to_record),
                        style = MaterialTheme.typography.bodySmall,
                        color = DreamColors.InkMuted,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(20.dp))
                    MoonPhaseLine(phase = phase)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.home_last_night, phase.label, phase.dayInLunation),
                        style = MaterialTheme.typography.labelSmall,
                        color = DreamColors.InkFaint,
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(Modifier.weight(1f))
                SectionLabel(
                    text = stringResource(R.string.home_ask_dream).uppercase(),
                    modifier = Modifier.fillMaxWidth(),
                )
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = DreamSpacing.md)
                        .clickable(onClick = onOracle),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DreamColors.IndigoSoft.copy(alpha = 0.85f),
                    ),
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = DreamSpacing.md, vertical = DreamSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Outlined.AutoAwesome,
                            contentDescription = null,
                            tint = DreamColors.AuroraSoft,
                            modifier = Modifier.size(28.dp),
                        )
                        Spacer(Modifier.width(DreamSpacing.sm))
                        Column(Modifier.weight(1f)) {
                            Text(
                                stringResource(R.string.home_ask_dream),
                                color = DreamColors.Moonglow,
                                style = MaterialTheme.typography.titleSmall,
                            )
                            Text(
                                stringResource(R.string.oracle_subtitle),
                                color = DreamColors.InkFaint,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 2,
                            )
                        }
                        Icon(
                            Icons.Outlined.ChevronRight,
                            contentDescription = stringResource(R.string.home_ask_dream),
                            tint = DreamColors.InkMuted,
                        )
                    }
                }
            }
        }
    }
}
