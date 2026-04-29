package com.charles.app.dreamloom.feature.detail

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.charles.app.dreamloom.R
import com.charles.app.dreamloom.data.model.formatDateLine
import com.charles.app.dreamloom.data.model.moodLabelForKey
import com.charles.app.dreamloom.data.model.symbolsList
import com.charles.app.dreamloom.data.model.toMoodDotColor
import com.charles.app.dreamloom.ui.components.AuroraStarfieldBackground
import com.charles.app.dreamloom.ui.components.SectionLabel
import com.charles.app.dreamloom.ui.theme.CormorantFont
import com.charles.app.dreamloom.ui.theme.DreamColors
import com.charles.app.dreamloom.ui.theme.DreamSpacing
import com.charles.app.dreamloom.ui.theme.BodyFont
import java.io.File
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DreamDetailScreen(
    @Suppress("UNUSED_PARAMETER") id: Long,
    onBack: () -> Unit,
    vm: DreamDetailViewModel = hiltViewModel(),
) {
    val dream by vm.dream.collectAsStateWithLifecycle()
    val reBusy by vm.reInterpreting.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = context as ComponentActivity
    var menuOpen by remember { mutableStateOf(false) }
    var deleteOpen by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { vm.preloadInterstitial() }

    fun handleBack() {
        if (reBusy) return
        scope.launch {
            if (vm.canShowInterstitialOnBack()) {
                vm.showInterstitialIfPermitted(activity) { shown ->
                    if (shown) {
                        vm.recordInterstitialShown()
                    }
                    onBack()
                }
            } else {
                onBack()
            }
        }
    }

    BackHandler(enabled = !reBusy) { handleBack() }

    // Defensive: clamp height so the inner LazyColumn(weight=1f) never gets infinite max
    // constraints (compose-animation can transiently propagate infinity through NavHost).
    val maxScreenHeight = LocalConfiguration.current.screenHeightDp.dp + 200.dp

    AuroraStarfieldBackground(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .heightIn(max = maxScreenHeight),
        ) {
            when (val d = dream) {
                null -> {
                    TopAppBar(
                        title = { Text("…") },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = DreamColors.Ink,
                            navigationIconContentColor = DreamColors.Moonglow,
                        ),
                        navigationIcon = {
                            IconButton(onClick = { onBack() }) {
                                Icon(
                                    Icons.Outlined.ArrowBack,
                                    stringResource(R.string.cd_back),
                                )
                            }
                        },
                    )
                }
                else -> {
                    Column(Modifier.fillMaxSize()) {
                    val (date, dow) = d.formatDateLine()
                    TopAppBar(
                        title = { Text("$date · $dow", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent,
                            titleContentColor = DreamColors.Ink,
                            navigationIconContentColor = DreamColors.Moonglow,
                        ),
                        navigationIcon = {
                            IconButton(onClick = { handleBack() }, enabled = !reBusy) {
                                Icon(
                                    Icons.Outlined.ArrowBack,
                                    stringResource(R.string.cd_back),
                                )
                            }
                        },
                        actions = {
                            Box {
                                IconButton(
                                    onClick = { menuOpen = true },
                                    enabled = !reBusy,
                                ) { Icon(Icons.Filled.MoreVert, stringResource(R.string.detail_menu_more)) }
                                DropdownMenu(
                                    expanded = menuOpen,
                                    onDismissRequest = { menuOpen = false },
                                ) {
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.detail_reinterpret)) },
                                        onClick = {
                                            menuOpen = false
                                            if (!reBusy) vm.reInterpret()
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.detail_share)) },
                                        onClick = {
                                            menuOpen = false
                                            scope.launch {
                                                val b = withContext(Dispatchers.Main) {
                                                    DreamShareImageRenderer.render(activity, d)
                                                }
                                                ShareDreamPng.openChooser(context, d.id, b)
                                            }
                                        },
                                    )
                                    DropdownMenuItem(
                                        text = { Text(stringResource(R.string.action_delete)) },
                                        onClick = { menuOpen = false; deleteOpen = true },
                                    )
                                }
                            }
                        },
                    )
                    if (reBusy) {
                        Box(
                            Modifier
                                .weight(1f, fill = true)
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = DreamColors.Moonglow)
                                Text(
                                    stringResource(R.string.detail_reweaving),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DreamColors.InkMuted,
                                    modifier = Modifier.padding(top = DreamSpacing.md),
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f, fill = true)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(
                                start = DreamSpacing.lg,
                                end = DreamSpacing.lg,
                                top = 0.dp,
                                bottom = DreamSpacing.xl,
                            ),
                            verticalArrangement = Arrangement.spacedBy(DreamSpacing.md),
                        ) {
                            item {
                                Text(
                                    text = d.title ?: "…",
                                    style = MaterialTheme.typography.headlineSmall.copy(fontFamily = CormorantFont),
                                    color = DreamColors.Ink,
                                )
                            }
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Text(
                                        moodLabelForKey(d.mood),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = DreamColors.InkMuted,
                                    )
                                    Box(
                                        Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(d.mood.toMoodDotColor()),
                                    )
                                }
                            }
                            item {
                                if (d.symbolsList().isNotEmpty()) {
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        d.symbolsList().forEach { s ->
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
                            }
                            d.photoPath?.let { path ->
                                if (File(path).exists()) {
                                    item {
                                        Card(
                                            modifier = Modifier.widthIn(max = 400.dp).fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                        ) {
                                            AsyncImage(
                                                model = File(path),
                                                contentDescription = stringResource(R.string.detail_cd_photo),
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(220.dp),
                                            )
                                        }
                                    }
                                }
                            }
                            item {
                                SectionLabel(
                                    stringResource(R.string.section_your_dream),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Text(
                                    text = "\"${d.rawText.trim()}\"",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontFamily = BodyFont,
                                        fontStyle = FontStyle.Italic,
                                    ),
                                    color = DreamColors.Ink,
                                )
                            }
                            item {
                                SectionLabel(
                                    stringResource(R.string.section_interpretation),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Text(
                                    d.interpretation?.trim() ?: "…",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = DreamColors.Ink,
                                )
                            }
                            item {
                                SectionLabel(
                                    stringResource(R.string.section_todays_intention),
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                val intention = d.intention?.trim().orEmpty()
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, DreamColors.Moonglow, RoundedCornerShape(12.dp)),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = androidx.compose.material3.CardDefaults.cardColors(
                                        containerColor = DreamColors.IndigoSoft.copy(alpha = 0.4f),
                                    ),
                                ) {
                                    Text(
                                        if (intention.isNotEmpty()) {
                                            intention
                                        } else {
                                            "—"
                                        },
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = DreamColors.Moonglow,
                                        modifier = Modifier.padding(DreamSpacing.md),
                                    )
                                }
                            }
                            item {
                                TextButton(
                                    onClick = {
                                        vm.showExtendedRewarded(activity) { earned ->
                                            val msg = if (earned) {
                                                "Extended interpretation unlocked."
                                            } else {
                                                "Ad unavailable right now. Try again in a moment."
                                            }
                                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                ) {
                                    Text("✦ Look deeper (watch short ad)")
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    }
    if (deleteOpen && dream != null) {
            AlertDialog(
                onDismissRequest = { deleteOpen = false },
                title = { Text(stringResource(R.string.detail_delete_title)) },
                text = { Text(stringResource(R.string.detail_delete_body)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            deleteOpen = false
                            vm.deleteDream { onBack() }
                        },
                    ) { Text(stringResource(R.string.action_delete)) }
                },
                dismissButton = {
                    TextButton(onClick = { deleteOpen = false }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                },
            )
    }
}
