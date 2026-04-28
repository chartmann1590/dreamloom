package com.charles.app.dreamloom.feature.atlas

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.ExploreOff
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Nightlight
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.charles.app.dreamloom.data.db.DreamEntity
import com.charles.app.dreamloom.data.model.formatDateLine
import com.charles.app.dreamloom.data.model.symbolsList
import com.charles.app.dreamloom.data.model.toMoodDotColor
import com.charles.app.dreamloom.ui.theme.DreamColors
import com.charles.app.dreamloom.ui.theme.DreamSpacing
import com.charles.app.dreamloom.ui.theme.CormorantFont

private fun moodGlyph(mood: String): ImageVector = when (mood) {
    "serene" -> Icons.Outlined.Spa
    "anxious" -> Icons.Outlined.Psychology
    "joyful" -> Icons.Outlined.WbSunny
    "lost" -> Icons.Outlined.ExploreOff
    "fierce" -> Icons.Outlined.LocalFireDepartment
    "skip" -> Icons.Outlined.Nightlight
    else -> Icons.Outlined.Bedtime
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalFoundationApi::class)
@Composable
fun DreamTimelineCard(
    dream: DreamEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (date, dow) = dream.formatDateLine()
    val preview = (dream.interpretation ?: dream.rawText)
        .lineSequence()
        .joinToString(" ")
        .let { text ->
            when {
                text.isBlank() -> "…"
                text.length > 200 -> text.take(200) + "…"
                else -> text
            }
        }
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
            ),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .background(DreamColors.Indigo.copy(alpha = 0.4f))
                .padding(DreamSpacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(dream.mood.toMoodDotColor().copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = moodGlyph(dream.mood),
                    contentDescription = null,
                    tint = dream.mood.toMoodDotColor(),
                    modifier = Modifier.size(22.dp),
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = DreamSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(DreamSpacing.xs),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelMedium,
                        color = DreamColors.Moonglow,
                    )
                    Text(
                        text = dow,
                        style = MaterialTheme.typography.labelSmall,
                        color = DreamColors.InkFaint,
                    )
                }
                Text(
                    text = dream.title ?: "…",
                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = CormorantFont),
                    color = DreamColors.Ink,
                )
                Text(
                    text = preview,
                    style = MaterialTheme.typography.bodySmall,
                    color = DreamColors.InkMuted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (dream.symbolsList().isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        dream.symbolsList().take(8).forEach { s ->
                            AssistChip(
                                onClick = {},
                                label = { Text(s) },
                                enabled = false,
                                border = null,
                                modifier = Modifier.padding(0.dp),
                                colors = AssistChipDefaults.assistChipColors(
                                    labelColor = DreamColors.Moonglow,
                                    containerColor = DreamColors.IndigoSoft.copy(alpha = 0.5f),
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}
