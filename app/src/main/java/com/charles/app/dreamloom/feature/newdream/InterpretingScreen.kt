package com.charles.app.dreamloom.feature.newdream

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.charles.app.dreamloom.R
import com.charles.app.dreamloom.llm.parseInterpretationStreaming
import com.charles.app.dreamloom.ui.components.LoomWeaveBackground
import com.charles.app.dreamloom.ui.theme.CormorantFont
import com.charles.app.dreamloom.ui.theme.DreamColors
import com.charles.app.dreamloom.ui.theme.DreamSpacing
import kotlin.math.min

@Composable
fun InterpretingScreen(
    dreamId: Long,
    onComplete: (Long) -> Unit,
    vm: InterpretingViewModel = hiltViewModel(),
) {
    val raw by vm.raw.collectAsState()
    val streamFinished by vm.streamFinished.collectAsState()
    val streamSuccess by vm.streamSuccess.collectAsState()
    val streamed = parseInterpretationStreaming(raw)
    val transition = rememberInfiniteTransition(label = "weaveText")
    val textPulse by transition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "textPulse",
    )
    val scroll = rememberScrollState()
    val chipScroll = rememberScrollState()
    val surface = DreamColors.Indigo.copy(alpha = 0.82f)
    val glowColor = DreamColors.Moonglow.copy(alpha = 0.12f * min(1f, textPulse))
    val headerText = if (streamFinished) {
        stringResource(R.string.interpreting_loom_done)
    } else {
        stringResource(R.string.interpreting_loom_title)
    }
    Box(Modifier.fillMaxSize()) {
        LoomWeaveBackground(Modifier.fillMaxSize())
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        0f to surface,
                        0.4f to DreamColors.Night.copy(alpha = 0.92f),
                        1f to DreamColors.Night,
                    ),
                ),
        )
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(horizontal = DreamSpacing.lg)
                .padding(top = DreamSpacing.xl, bottom = DreamSpacing.xxl)
                .drawBehind {
                    drawLine(
                        color = glowColor,
                        start = Offset(0f, 40f),
                        end = Offset(size.width, 80f),
                        strokeWidth = 1f,
                    )
                },
        ) {
            Text(
                headerText,
                style = MaterialTheme.typography.labelMedium,
                color = DreamColors.Moonglow,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(if (streamFinished) 0.75f else 0.95f),
            )
            if (!streamSuccess && streamFinished) {
                Spacer(Modifier.height(DreamSpacing.xs))
                Text(
                    "The weave was interrupted — you can re-interpret from the dream page.",
                    style = MaterialTheme.typography.bodySmall,
                    color = DreamColors.InkMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = DreamSpacing.sm),
                )
            }
            Spacer(Modifier.height(DreamSpacing.lg))
            LoomSectionLabel(
                stringResource(R.string.section_title),
                streamed.title != null,
            )
            StreamedText(
                text = streamed.title,
                pulse = if (!streamFinished) textPulse else 1f,
                largeTitle = true,
                streamDone = streamFinished,
            )
            Spacer(Modifier.height(DreamSpacing.md))
            LoomSectionLabel(
                stringResource(R.string.section_symbols),
                streamed.symbols.isNotEmpty(),
            )
            if (streamed.symbols.isNotEmpty()) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(chipScroll),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    streamed.symbols.forEach { s ->
                        AssistChip(
                            onClick = { },
                            label = { Text(s) },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = DreamColors.MoonglowSoft,
                                containerColor = DreamColors.Violet.copy(alpha = 0.3f),
                            ),
                        )
                    }
                }
            } else {
                LoomSectionSymbolsPlaceholder(streamFinished)
            }
            Spacer(Modifier.height(DreamSpacing.md))
            LoomSectionLabel(
                stringResource(R.string.section_interpretation),
                streamed.interpretation != null,
            )
            StreamedText(
                text = streamed.interpretation,
                pulse = if (!streamFinished) textPulse else 1f,
                streamDone = streamFinished,
            )
            Spacer(Modifier.height(DreamSpacing.md))
            LoomSectionLabel(
                stringResource(R.string.section_intention),
                streamed.intention != null,
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(
                        DreamColors.IndigoSoft.copy(alpha = 0.4f),
                        RoundedCornerShape(14.dp),
                    )
                    .padding(DreamSpacing.md),
            ) {
                StreamedText(
                    text = streamed.intention,
                    pulse = if (!streamFinished) textPulse else 1f,
                    isIntention = true,
                    streamDone = streamFinished,
                )
            }
            if (streamFinished) {
                Spacer(Modifier.height(DreamSpacing.xl))
                Button(
                    onClick = { onComplete(dreamId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(0.88f + 0.12f * min(1f, textPulse)),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DreamColors.Moonglow,
                        contentColor = DreamColors.Night,
                    ),
                ) { Text(stringResource(R.string.interpreting_save_view)) }
            } else {
                Spacer(Modifier.height(DreamSpacing.huge))
            }
        }
    }
}

@Composable
private fun LoomSectionLabel(label: String, hasContent: Boolean) {
    Text(
        "──  $label  ──",
        style = MaterialTheme.typography.labelSmall,
        color = if (hasContent) DreamColors.Moonglow.copy(alpha = 0.7f) else DreamColors.InkFaint,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    )
}

@Composable
private fun LoomSectionSymbolsPlaceholder(streamDone: Boolean) {
    Text(
        if (streamDone) "—" else "…",
        style = MaterialTheme.typography.bodySmall,
        color = DreamColors.InkFaint.copy(alpha = 0.45f),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    )
}

@Composable
private fun StreamedText(
    text: String?,
    pulse: Float,
    isIntention: Boolean = false,
    largeTitle: Boolean = false,
    streamDone: Boolean = false,
) {
    val t = text?.ifBlank { null }
    if (t == null) {
        LoomTextPlaceholder(streamDone)
    } else {
        val style = when {
            largeTitle -> MaterialTheme.typography.headlineSmall
            isIntention -> MaterialTheme.typography.bodyLarge
            else -> MaterialTheme.typography.bodyLarge
        }
        Text(
            t,
            style = style,
            color = if (isIntention) DreamColors.Moonglow else DreamColors.Ink,
            textAlign = TextAlign.Start,
            lineHeight = style.lineHeight * 1.32f,
            fontFamily = CormorantFont,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(0.85f + 0.15f * min(1f, pulse)),
        )
    }
}

@Composable
private fun LoomTextPlaceholder(streamDone: Boolean) {
    Text(
        if (streamDone) "—" else "…",
        style = MaterialTheme.typography.bodySmall,
        color = DreamColors.InkFaint.copy(alpha = 0.45f),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
    )
}
