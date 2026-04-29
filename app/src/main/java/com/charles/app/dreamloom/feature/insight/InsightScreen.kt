package com.charles.app.dreamloom.feature.insight

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charles.app.dreamloom.R
import com.charles.app.dreamloom.ads.NativeAdCard
import com.charles.app.dreamloom.data.db.DreamEntity
import com.charles.app.dreamloom.data.model.toMoodDotColor
import com.charles.app.dreamloom.data.model.topSymbolCountsFromInsightJson
import com.charles.app.dreamloom.ui.components.AuroraStarfieldBackground
import com.charles.app.dreamloom.ui.components.SectionLabel
import com.charles.app.dreamloom.ui.theme.DreamColors
import com.charles.app.dreamloom.ui.theme.DreamSpacing
import java.text.DateFormat
import java.util.Date

@Composable
fun InsightScreen(
    vm: InsightViewModel = hiltViewModel(),
) {
    val insight by vm.latestInsight.collectAsStateWithLifecycle()
    val strip by vm.recentStripDreams.collectAsStateWithLifecycle()
    val lastRunAtMs by vm.lastRunAtMs.collectAsStateWithLifecycle()
    val nextRunAtMs by vm.nextRunAtMs.collectAsStateWithLifecycle()
    val runStatus by vm.runStatus.collectAsStateWithLifecycle()
    val scroll = rememberScrollState()
    val context = LocalContext.current
    val activity = context as ComponentActivity
    val dateTimeFormatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT)
    LaunchedEffect(Unit) { vm.preloadRewarded() }

    AuroraStarfieldBackground(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = DreamSpacing.md)
                .padding(top = DreamSpacing.lg, bottom = DreamSpacing.lg)
                .verticalScroll(scroll),
            verticalArrangement = Arrangement.spacedBy(DreamSpacing.md),
        ) {
            Text(
                text = stringResource(R.string.insight_title),
                style = MaterialTheme.typography.headlineSmall,
                color = DreamColors.Moonglow,
            )
            Text(
                text = stringResource(R.string.insight_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = DreamColors.InkFaint,
            )
            Text(
                text = stringResource(
                    R.string.insight_last_run,
                    lastRunAtMs?.let { dateTimeFormatter.format(Date(it)) } ?: stringResource(R.string.insight_never_run),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = DreamColors.InkMuted,
            )
            Text(
                text = stringResource(
                    R.string.insight_next_run,
                    dateTimeFormatter.format(Date(nextRunAtMs)),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = DreamColors.InkMuted,
            )
            Button(
                onClick = vm::runNow,
                modifier = Modifier.fillMaxWidth(),
                enabled = !runStatus.isRunning,
            ) {
                Text(if (runStatus.isRunning) stringResource(R.string.insight_running_now) else stringResource(R.string.insight_run_now))
            }
            Button(
                onClick = {
                    vm.showWeeklyRewarded(activity) { earned ->
                        val msg = if (earned) {
                            "Thanks for supporting Dreamloom."
                        } else {
                            "Ad unavailable right now. Try again shortly."
                        }
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !runStatus.isRunning,
            ) {
                Text("✦ Weave this week (watch short ad)")
            }
            if (runStatus.isRunning) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            if (!runStatus.note.isNullOrBlank()) {
                Text(
                    text = runStatus.note!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = DreamColors.InkMuted,
                )
            }
            if (strip.isNotEmpty()) {
                SectionLabel(stringResource(R.string.insight_mood_strip).uppercase())
                MoodStrip(Modifier.fillMaxWidth(), strip)
            }
            if (insight == null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = DreamSpacing.sm),
                    colors = CardDefaults.cardColors(
                        containerColor = DreamColors.IndigoSoft.copy(alpha = 0.55f),
                    ),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(DreamSpacing.lg),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AutoGraph,
                            contentDescription = null,
                            modifier = Modifier.size(52.dp),
                            tint = DreamColors.AuroraSoft,
                        )
                        Spacer(Modifier.height(DreamSpacing.sm))
                        Text(
                            stringResource(R.string.insight_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = DreamColors.InkMuted,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            } else {
                val i = insight!!
                val top = topSymbolCountsFromInsightJson(i.topSymbolsJson)
                if (top.isNotEmpty()) {
                    SectionLabel(stringResource(R.string.insight_symbols).uppercase())
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                    ) {
                        for ((sym, c) in top) {
                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        if (c > 1) "$sym ×$c" else sym,
                                        color = DreamColors.Ink,
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = DreamColors.IndigoSoft,
                                ),
                            )
                        }
                    }
                }
                SectionLabel(stringResource(R.string.insight_pattern).uppercase())
                Text(
                    i.pattern,
                    style = MaterialTheme.typography.bodyLarge,
                    color = DreamColors.MoonglowSoft,
                )
                SectionLabel(stringResource(R.string.insight_summary).uppercase())
                Text(
                    i.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DreamColors.Ink,
                )
                SectionLabel(stringResource(R.string.insight_invitation).uppercase())
                Text(
                    i.invitation,
                    style = MaterialTheme.typography.titleSmall,
                    color = DreamColors.AuroraSoft,
                )
            }
            NativeAdCard(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun MoodStrip(modifier: Modifier, strip: List<DreamEntity>) {
    Row(
        modifier
            .height(12.dp)
            .clip(RoundedCornerShape(3.dp)),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        for (d in strip) {
            Box(
                Modifier
                    .weight(1f)
                    .height(12.dp)
                    .background(d.mood.toMoodDotColor(), RoundedCornerShape(2.dp)),
            )
        }
    }
}
