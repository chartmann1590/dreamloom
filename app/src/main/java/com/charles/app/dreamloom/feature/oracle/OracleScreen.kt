package com.charles.app.dreamloom.feature.oracle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charles.app.dreamloom.R
import com.charles.app.dreamloom.ads.NativeAdCard
import com.charles.app.dreamloom.ui.components.AuroraStarfieldBackground
import com.charles.app.dreamloom.ui.theme.DreamColors
import com.charles.app.dreamloom.ui.theme.DreamSpacing
import java.text.DateFormat
import java.util.Date

@Composable
fun OracleScreen(
    vm: OracleViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val list by vm.oracles.collectAsStateWithLifecycle()
    val busy by vm.busy.collectAsStateWithLifecycle()
    val err by vm.error.collectAsStateWithLifecycle()
    var q by rememberSaveable { mutableStateOf("") }
    val df = remember { DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT) }

    AuroraStarfieldBackground(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = DreamSpacing.md, vertical = DreamSpacing.md),
            verticalArrangement = Arrangement.spacedBy(DreamSpacing.sm),
        ) {
            RowBar(onBack, stringResource(R.string.oracle_title))
            Text(
                stringResource(R.string.oracle_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = DreamColors.InkFaint,
            )
            if (err != null) {
                Text(
                    err!!,
                    color = DreamColors.Danger,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(DreamSpacing.md),
            ) {
                items(list, key = { it.id }) { row ->
                    Column(Modifier.fillMaxWidth()) {
                        Text(
                            df.format(Date(row.createdAt)),
                            style = MaterialTheme.typography.labelSmall,
                            color = DreamColors.InkFaint,
                        )
                        Text(
                            "“${row.question}”",
                            style = MaterialTheme.typography.bodyMedium,
                            color = DreamColors.Moonglow,
                        )
                        Text(
                            row.answer,
                            style = MaterialTheme.typography.bodyMedium,
                            color = DreamColors.Ink,
                        )
                    }
                }
                if (list.isNotEmpty()) {
                    item(key = "oracle_native_ad") {
                        NativeAdCard(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
            OutlinedTextField(
                value = q,
                onValueChange = { q = it; if (err != null) vm.clearError() },
                enabled = !busy,
                label = { Text(stringResource(R.string.oracle_question)) },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = { vm.ask(q) },
                enabled = !busy && q.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (busy) stringResource(R.string.oracle_asking) else stringResource(R.string.oracle_send),
                )
            }
        }
    }
}

@Composable
private fun RowBar(onBack: () -> Unit, title: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.Outlined.ArrowBack,
                stringResource(R.string.cd_back),
                tint = DreamColors.Moonglow,
            )
        }
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = DreamColors.Moonglow,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f),
        )
    }
}
