package com.charles.app.dreamloom.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charles.app.dreamloom.ui.theme.DreamColors

/**
 * "─── LABEL ───" from [design/SYSTEM.md](file://../design/SYSTEM.md)
 */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
    lineColor: Color = DreamColors.InkFaint.copy(alpha = 0.3f),
) {
    val style = MaterialTheme.typography.labelSmall
    val spacing = 6.dp
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(lineColor),
        )
        Text(
            text = text,
            style = style.copy(letterSpacing = 1.5.sp),
            color = DreamColors.InkFaint,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = spacing),
        )
        Spacer(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(lineColor),
        )
    }
}
