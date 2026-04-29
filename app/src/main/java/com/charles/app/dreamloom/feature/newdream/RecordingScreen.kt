package com.charles.app.dreamloom.feature.newdream

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.charles.app.dreamloom.R
import com.charles.app.dreamloom.ui.components.AuroraStarfieldBackground
import com.charles.app.dreamloom.ui.components.VoiceWaveformBar
import com.charles.app.dreamloom.ui.theme.CormorantFont
import com.charles.app.dreamloom.ui.theme.DreamColors
import com.charles.app.dreamloom.ui.theme.DreamSpacing
import android.content.pm.PackageManager
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts

private data class MoodItem(val id: String, val emoji: String, val color: androidx.compose.ui.graphics.Color)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordingScreen(
    onBack: () -> Unit,
    onInterpreting: (Long) -> Unit,
    vm: RecordingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var isTextMode by remember { mutableStateOf(false) }
    var isListening by remember { mutableStateOf(false) }
    var transcript by remember { mutableStateOf("") }
    var errorHint by remember { mutableStateOf<String?>(null) }
    var pendingPhoto by remember { mutableStateOf<Uri?>(null) }
    var minLengthError by remember { mutableStateOf(false) }
    var selectedMood by remember { mutableStateOf("skip") }
    val autoStopAfterSilence by vm.autoStopAfterSilence.collectAsStateWithLifecycle()

    val pickPhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { u -> pendingPhoto = u }

    val voice = remember(autoStopAfterSilence) {
        VoiceTranscription(
            appContext = context.applicationContext,
            onUpdate = { t ->
                transcript = t
                minLengthError = false
            },
            onError = { msg -> errorHint = msg },
            silenceAutoStopMs = if (autoStopAfterSilence) 30_000L else 0L,
        )
    }

    DisposableEffect(voice) {
        onDispose { voice.release() }
    }

    val requestMic = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            if (voice.isAvailable) {
                voice.startListening()
                isListening = true
            }
        } else {
            errorHint = context.getString(R.string.recording_mic_denied)
        }
    }

    val moods = remember {
        listOf(
            MoodItem("serene", "😌", DreamColors.MoodSerene),
            MoodItem("anxious", "😰", DreamColors.MoodAnxious),
            MoodItem("joyful", "🌤", DreamColors.MoodJoyful),
            MoodItem("lost", "🌫", DreamColors.MoodLost),
            MoodItem("fierce", "🔥", DreamColors.MoodFierce),
            MoodItem("skip", "·", DreamColors.InkFaint),
        )
    }

    val scroll = rememberScrollState()

    // Defensive: clamp height so the inner weight(1f).verticalScroll never gets infinite max
    // constraints (compose-animation can transiently propagate infinity through NavHost).
    val maxScreenHeight = LocalConfiguration.current.screenHeightDp.dp + 200.dp

    AuroraStarfieldBackground(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .heightIn(max = maxScreenHeight),
        ) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        stringResource(R.string.new_dream_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = DreamColors.Moonglow,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Outlined.ArrowBack,
                            stringResource(R.string.back),
                            tint = DreamColors.InkMuted,
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (isListening) {
                                voice.stopListening()
                                isListening = false
                            }
                            isTextMode = !isTextMode
                        },
                    ) {
                        Icon(
                            Icons.Outlined.Keyboard,
                            stringResource(R.string.recording_type_instead),
                            tint = if (isTextMode) DreamColors.Moonglow else DreamColors.InkMuted,
                        )
                    }
                    IconButton(
                        onClick = {
                            pickPhoto.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly,
                                ),
                            )
                        },
                    ) {
                        Icon(
                            Icons.Outlined.AddPhotoAlternate,
                            stringResource(R.string.recording_add_photo),
                            tint = DreamColors.Moonglow,
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                ),
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = DreamSpacing.lg)
                    .verticalScroll(scroll),
            ) {
                errorHint?.let { hint ->
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.bodySmall,
                        color = DreamColors.Moonglow.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(bottom = DreamSpacing.sm),
                    )
                }
                if (pendingPhoto != null) {
                    Box(
                        Modifier
                            .fillMaxWidth(0.45f)
                            .height(120.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .align(Alignment.CenterHorizontally)
                            .shadow(8.dp, RoundedCornerShape(12.dp))
                            .background(DreamColors.Indigo.copy(alpha = 0.3f)),
                    ) {
                        AsyncImage(
                            model = pendingPhoto,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                        )
                    }
                    Spacer(Modifier.height(DreamSpacing.md))
                }
                if (isTextMode) {
                    OutlinedTextField(
                        value = transcript,
                        onValueChange = { transcript = it; minLengthError = false; errorHint = null },
                        label = { Text(stringResource(R.string.dream_placeholder)) },
                        minLines = 4,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DreamColors.Moonglow.copy(alpha = 0.6f),
                            unfocusedBorderColor = DreamColors.InkFaint,
                            focusedLabelColor = DreamColors.Moonglow,
                            focusedTextColor = DreamColors.MoonglowSoft,
                            unfocusedTextColor = DreamColors.MoonglowSoft,
                            cursorColor = DreamColors.Moonglow,
                        ),
                        textStyle = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = CormorantFont,
                            color = DreamColors.MoonglowSoft,
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                        ),
                    )
                } else {
                    Text(
                        text = transcript.ifBlank { stringResource(R.string.dream_placeholder) },
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (transcript.isBlank()) DreamColors.InkFaint else DreamColors.MoonglowSoft,
                        fontFamily = CormorantFont,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = DreamSpacing.sm)
                            .then(
                                Modifier.background(
                                    DreamColors.Indigo.copy(alpha = 0.2f),
                                    RoundedCornerShape(12.dp),
                                ),
                            )
                            .padding(DreamSpacing.md),
                    )
                }
                if (!isTextMode) {
                    Spacer(Modifier.height(DreamSpacing.lg))
                    VoiceWaveformBar(
                        isActive = isListening,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(DreamSpacing.md))
                    FilledTonalButton(
                        onClick = {
                            if (!voice.isAvailable) {
                                errorHint = context.getString(R.string.recording_voice_unavailable)
                                isTextMode = true
                                return@FilledTonalButton
                            }
                            errorHint = null
                            if (ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO,
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                requestMic.launch(Manifest.permission.RECORD_AUDIO)
                                return@FilledTonalButton
                            }
                            if (isListening) {
                                voice.stopListening()
                                isListening = false
                            } else {
                                voice.startListening()
                                isListening = true
                            }
                        },
                        shape = CircleShape,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isListening) {
                                DreamColors.Danger.copy(alpha = 0.4f)
                            } else {
                                DreamColors.IndigoSoft
                            },
                            contentColor = DreamColors.Ink,
                        ),
                        modifier = Modifier
                            .size(88.dp)
                            .align(Alignment.CenterHorizontally),
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = stringResource(R.string.recording_mic))
                    }
                }
                Spacer(Modifier.height(DreamSpacing.xl))
                Text(
                    stringResource(R.string.mood_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = DreamColors.InkMuted,
                )
                Spacer(Modifier.height(DreamSpacing.sm))
                val rows = moods.chunked(3)
                rows.forEach { rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(DreamSpacing.xs, Alignment.CenterHorizontally),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    ) {
                        rowItems.forEach { m ->
                            val sel = selectedMood == m.id
                            AssistChip(
                                onClick = { selectedMood = m.id; vm.mood = m.id },
                                label = {
                                    Text(
                                        if (m.id == "skip") "Skip" else "${m.emoji}  ${m.id.replaceFirstChar { c -> c.uppercase() }}",
                                        style = MaterialTheme.typography.labelMedium,
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (sel) m.color.copy(alpha = 0.35f) else DreamColors.Indigo.copy(alpha = 0.2f),
                                    labelColor = DreamColors.Ink,
                                ),
                            )
                        }
                    }
                }
                if (minLengthError) {
                    Spacer(Modifier.height(DreamSpacing.xs))
                    Text(
                        stringResource(R.string.recording_min_length),
                        color = DreamColors.Danger,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Spacer(Modifier.height(DreamSpacing.lg))
                Button(
                    onClick = {
                        if (transcript.trim().length < 10) {
                            minLengthError = true
                            return@Button
                        }
                        vm.mood = selectedMood
                        vm.text = transcript.trim()
                        vm.saveAndGo(pendingPhoto) { onInterpreting(it) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = DreamSpacing.xxl),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DreamColors.Moonglow.copy(alpha = 0.85f),
                        contentColor = DreamColors.Night,
                    ),
                    shape = RoundedCornerShape(28.dp),
                ) {
                    Text(
                        stringResource(R.string.save_dream),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
