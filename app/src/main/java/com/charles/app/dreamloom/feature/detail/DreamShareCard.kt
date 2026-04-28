package com.charles.app.dreamloom.feature.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.charles.app.dreamloom.data.db.DreamEntity
import com.charles.app.dreamloom.data.model.formatDateLine
import com.charles.app.dreamloom.data.model.symbolsList
import com.charles.app.dreamloom.ui.theme.DreamColors
import com.charles.app.dreamloom.ui.theme.DreamSpacing
import com.charles.app.dreamloom.ui.theme.CormorantFont

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DreamShareCard(
    dream: DreamEntity,
    modifier: Modifier = Modifier,
) {
    val (date, dow) = dream.formatDateLine()
    val preview = (dream.interpretation ?: dream.rawText)
        .lineSequence()
        .take(2)
        .joinToString(" ")
        .let { if (it.length > 360) it.take(360) + "…" else it }
    Column(
        modifier
            .fillMaxWidth()
            .background(DreamColors.Indigo)
            .padding(DreamSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(DreamSpacing.sm),
    ) {
        Text(
            text = "$date · $dow",
            style = MaterialTheme.typography.labelSmall,
            color = DreamColors.Moonglow,
        )
        Text(
            text = dream.title ?: "…",
            style = MaterialTheme.typography.headlineSmall.copy(fontFamily = CormorantFont),
            color = DreamColors.Ink,
            maxLines = 3,
        )
        if (dream.symbolsList().isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                dream.symbolsList().forEach { s ->
                    AssistChip(
                        onClick = {},
                        label = { Text(s) },
                        enabled = false,
                        border = null,
                        colors = AssistChipDefaults.assistChipColors(
                            labelColor = DreamColors.Moonglow,
                            containerColor = DreamColors.IndigoSoft,
                        ),
                    )
                }
            }
        }
        Text(
            text = preview,
            style = MaterialTheme.typography.bodyMedium,
            color = DreamColors.Ink.copy(alpha = 0.9f),
            maxLines = 6,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "Dreamloom",
            style = MaterialTheme.typography.labelSmall,
            color = DreamColors.InkFaint,
            modifier = Modifier.padding(top = DreamSpacing.xs),
        )
    }
}
