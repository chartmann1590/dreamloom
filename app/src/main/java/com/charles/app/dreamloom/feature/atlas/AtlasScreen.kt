package com.charles.app.dreamloom.feature.atlas

import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.charles.app.dreamloom.R
import com.charles.app.dreamloom.ads.NativeAdCard
import com.charles.app.dreamloom.data.model.dreamMoodOptionKeys
import com.charles.app.dreamloom.data.model.moodLabelForKey
import com.charles.app.dreamloom.feature.detail.DreamShareImageRenderer
import com.charles.app.dreamloom.feature.detail.ShareDreamPng
import com.charles.app.dreamloom.ui.components.AuroraStarfieldBackground
import com.charles.app.dreamloom.ui.theme.DreamColors
import com.charles.app.dreamloom.ui.theme.DreamSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AtlasScreen(
    onOpenDream: (Long) -> Unit,
    vm: AtlasViewModel = hiltViewModel(),
) {
    val dreams by vm.filteredDreams.collectAsStateWithLifecycle()
    val symbolIndex by vm.symbolIndex.collectAsStateWithLifecycle()
    val search by vm.searchText.collectAsStateWithLifecycle()
    val timeFilter by vm.timeFilter.collectAsStateWithLifecycle()
    val symFilter by vm.symbolFilter.collectAsStateWithLifecycle()
    val moodFilter by vm.moodFilter.collectAsStateWithLifecycle()
    var symSheet by remember { mutableStateOf(false) }
    var moodSheet by remember { mutableStateOf(false) }
    var longPressDreamId by remember { mutableStateOf<Long?>(null) }
    var deleteId by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current as ComponentActivity
    val shareJob: (Long) -> Unit = { id ->
        scope.launch {
            val d = withContext(Dispatchers.IO) { vm.getDream(id) } ?: return@launch
            val bmp = withContext(Dispatchers.Main) { DreamShareImageRenderer.render(activity, d) }
            ShareDreamPng.openChooser(activity, d.id, bmp)
        }
    }
    AuroraStarfieldBackground(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .imePadding()
                .padding(horizontal = DreamSpacing.md, vertical = DreamSpacing.md),
        ) {
            OutlinedTextField(
                value = search,
                onValueChange = { vm.searchText.value = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(stringResource(R.string.atlas_search_label)) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (search.isNotEmpty()) {
                        IconButton(onClick = { vm.searchText.value = "" }) {
                            Icon(
                                Icons.Outlined.Clear,
                                contentDescription = stringResource(R.string.cd_clear_search),
                            )
                        }
                    }
                },
            )
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = DreamSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                item {
                    FilterChip(
                        selected = timeFilter == AtlasTimeFilter.All,
                        onClick = { vm.timeFilter.value = AtlasTimeFilter.All },
                        label = { Text(stringResource(R.string.atlas_filter_all)) },
                    )
                }
                item {
                    FilterChip(
                        selected = timeFilter == AtlasTimeFilter.Week,
                        onClick = { vm.timeFilter.value = AtlasTimeFilter.Week },
                        label = { Text(stringResource(R.string.atlas_filter_week)) },
                    )
                }
                item {
                    FilterChip(
                        selected = timeFilter == AtlasTimeFilter.Month,
                        onClick = { vm.timeFilter.value = AtlasTimeFilter.Month },
                        label = { Text(stringResource(R.string.atlas_filter_month)) },
                    )
                }
                item {
                    val sym = symFilter
                    FilterChip(
                        selected = sym != null,
                        onClick = { symSheet = true },
                        label = {
                            Text(
                                if (sym == null) {
                                    stringResource(R.string.atlas_filter_symbol)
                                } else {
                                    sym
                                },
                            )
                        },
                        leadingIcon = { Icon(Icons.Filled.Spa, null) },
                    )
                }
                item {
                    val m = moodFilter
                    FilterChip(
                        selected = m != null,
                        onClick = { moodSheet = true },
                        label = {
                            Text(
                                if (m == null) {
                                    stringResource(R.string.atlas_filter_mood)
                                } else {
                                    moodLabelForKey(m)
                                },
                            )
                        },
                        leadingIcon = { Icon(Icons.Filled.Mood, null) },
                    )
                }
            }
            if (symFilter != null || moodFilter != null) {
                TextButton(
                    onClick = {
                        vm.symbolFilter.value = null
                        vm.moodFilter.value = null
                    },
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.FilterList, contentDescription = null)
                        Spacer(Modifier.width(4.dp))
                        Text(stringResource(R.string.atlas_clear_symbol_mood))
                    }
                }
            }
            if (dreams.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = DreamSpacing.lg),
                ) {
                    Image(
                        painter = painterResource(R.drawable.ill_empty_journal),
                        contentDescription = stringResource(R.string.cd_atlas_empty_ill),
                        modifier = Modifier
                            .size(width = 200.dp, height = 160.dp),
                        contentScale = ContentScale.Fit,
                    )
                    Text(
                        stringResource(R.string.atlas_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = DreamColors.InkMuted,
                        modifier = Modifier.padding(top = DreamSpacing.md, start = DreamSpacing.sm, end = DreamSpacing.sm),
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = DreamSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(DreamSpacing.sm),
                ) {
                    items(dreams, key = { it.id }) { d ->
                        DreamTimelineCard(
                            dream = d,
                            onClick = { onOpenDream(d.id) },
                            onLongClick = { longPressDreamId = d.id },
                        )
                    }
                    item(key = "atlas_native_ad") {
                        NativeAdCard(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
    if (symSheet) {
        ModalBottomSheet(
            onDismissRequest = { symSheet = false },
        ) {
            if (symbolIndex.isEmpty()) {
                Text(
                    stringResource(R.string.atlas_no_symbols),
                    modifier = Modifier.padding(DreamSpacing.lg),
                )
            } else {
                LazyColumn {
                    item {
                        TextButton(
                            onClick = {
                                vm.symbolFilter.value = null
                                symSheet = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(stringResource(R.string.atlas_symbol_any))
                        }
                    }
                    items(symbolIndex, key = { it.first }) { (sym, count) ->
                        TextButton(
                            onClick = {
                                vm.symbolFilter.value = sym
                                symSheet = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text("$sym  ($count)")
                        }
                    }
                }
            }
        }
    }
    if (moodSheet) {
        ModalBottomSheet(
            onDismissRequest = { moodSheet = false },
        ) {
            LazyColumn {
                item {
                    TextButton(
                        onClick = {
                            vm.moodFilter.value = null
                            moodSheet = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.atlas_mood_any))
                    }
                }
                items(dreamMoodOptionKeys) { k ->
                    TextButton(
                        onClick = {
                            vm.moodFilter.value = k
                            moodSheet = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(moodLabelForKey(k))
                    }
                }
            }
        }
    }
    longPressDreamId?.let { id ->
        AlertDialog(
            onDismissRequest = { longPressDreamId = null },
            title = { Text(stringResource(R.string.atlas_long_press_title)) },
            text = { Text(stringResource(R.string.atlas_long_press_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        longPressDreamId = null
                        shareJob(id)
                    },
                ) { Text(stringResource(R.string.action_share)) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        deleteId = id
                        longPressDreamId = null
                    },
                ) { Text(stringResource(R.string.action_delete)) }
            },
        )
    }
    deleteId?.let { id ->
        AlertDialog(
            onDismissRequest = { deleteId = null },
            title = { Text(stringResource(R.string.detail_delete_title)) },
            text = { Text(stringResource(R.string.detail_delete_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteDream(id)
                        deleteId = null
                    },
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { deleteId = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}
