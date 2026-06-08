package com.charles.app.dreamloom.feature.settings

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Comment
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Launch
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.charles.app.dreamloom.data.feedback.BugReport
import com.charles.app.dreamloom.ui.theme.DreamColors
import com.charles.app.dreamloom.ui.theme.DreamSpacing

@Composable
fun SupportFeedbackSection(
    viewModel: FeedbackViewModel,
    modifier: Modifier = Modifier
) {
    val reports by viewModel.bugReports.collectAsState()
    var showReportDialog by remember { mutableStateOf(false) }
    var selectedReportForDetails by remember { mutableStateOf<BugReport?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DreamColors.IndigoSoft.copy(alpha = 0.35f),
        ),
    ) {
        Column(
            modifier = Modifier.padding(DreamSpacing.md),
            verticalArrangement = Arrangement.spacedBy(DreamSpacing.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.BugReport,
                        contentDescription = null,
                        tint = DreamColors.Moonglow
                    )
                    Text(
                        text = "Support & Feedback",
                        style = MaterialTheme.typography.titleMedium,
                        color = DreamColors.Moonglow
                    )
                }

                Button(
                    onClick = { showReportDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DreamColors.AuroraSoft,
                        contentColor = DreamColors.Ink
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Report a Problem", style = MaterialTheme.typography.labelMedium)
                }
            }

            Text(
                text = "View and reply to your previously submitted issues, or file a new one. Submitted reports link to our GitHub repository issue tracker.",
                style = MaterialTheme.typography.bodySmall,
                color = DreamColors.InkMuted
            )

            if (reports.isNotEmpty()) {
                HorizontalDivider(color = DreamColors.InkFaint.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 4.dp))
                
                reports.forEach { report ->
                    BugReportRow(
                        report = report,
                        onClick = { selectedReportForDetails = report }
                    )
                }
            }
        }
    }

    if (showReportDialog) {
        ReportProblemDialog(
            viewModel = viewModel,
            onDismiss = { showReportDialog = false }
        )
    }

    selectedReportForDetails?.let { report ->
        IssueDetailsDialog(
            report = report,
            viewModel = viewModel,
            onDismiss = { selectedReportForDetails = null }
        )
    }
}

@Composable
fun BugReportRow(
    report: BugReport,
    onClick: () -> Unit
) {
    val statusColor = if (report.status.equals("open", ignoreCase = true)) {
        DreamColors.Success
    } else {
        DreamColors.Danger
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DreamColors.IndigoSoft.copy(alpha = 0.2f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = report.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = DreamColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Issue #${report.number} · ${formatDate(report.createdAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = DreamColors.InkMuted
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .border(1.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .background(statusColor.copy(alpha = 0.1f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = report.status.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }
}

@Composable
fun ReportProblemDialog(
    viewModel: FeedbackViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var includeDiagnostics by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val submitError by viewModel.submitError.collectAsState()
    val submitSuccess by viewModel.submitSuccess.collectAsState()

    val context = LocalContext.current
    val isConfigValid = viewModel.hasValidConfig()

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> imageUri = uri }
    )

    LaunchedEffect(submitSuccess) {
        if (submitSuccess) {
            viewModel.resetSubmitState()
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = DreamColors.NightDeep,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(DreamSpacing.md)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Report a Problem",
                        style = MaterialTheme.typography.titleLarge,
                        color = DreamColors.Moonglow,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = DreamColors.InkMuted)
                    }
                }

                HorizontalDivider(color = DreamColors.InkFaint.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

                // Scrollable Form Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Privacy Warning Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, DreamColors.Danger.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .background(DreamColors.Danger.copy(alpha = 0.05f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Your report will be submitted to this app’s GitHub issue tracker. Do not include passwords, private keys, medical information, financial information, or anything you do not want visible to the repository maintainers. If this repository is public, your report may be publicly visible. If you select a screenshot, make sure it does not contain private information.",
                            style = MaterialTheme.typography.bodySmall,
                            color = DreamColors.Danger,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (!isConfigValid) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, DreamColors.Danger.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .background(DreamColors.Danger.copy(alpha = 0.15f))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Configuration Error: Bug reporting is currently disabled because the GitHub repository parameters or token are not configured.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DreamColors.Ink,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    submitError?.let { err ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DreamColors.Danger.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                                .padding(8.dp)
                        ) {
                            Text(
                                text = err,
                                style = MaterialTheme.typography.bodySmall,
                                color = DreamColors.Danger
                            )
                        }
                    }

                    // Inputs
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title / Subject (Required)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSubmitting && isConfigValid,
                        colors = getTextFieldColors()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description of the problem (Required)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        enabled = !isSubmitting && isConfigValid,
                        colors = getTextFieldColors()
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isSubmitting && isConfigValid,
                        colors = getTextFieldColors()
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isSubmitting && isConfigValid,
                        colors = getTextFieldColors()
                    )

                    // Attach Screenshot Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            enabled = !isSubmitting && isConfigValid,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DreamColors.IndigoSoft,
                                contentColor = DreamColors.Ink
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.AttachFile, contentDescription = null, modifier = Modifier.size(18.dp))
                                Text("Attach Screenshot")
                            }
                        }

                        if (imageUri != null) {
                            TextButton(
                                onClick = { imageUri = null },
                                enabled = !isSubmitting,
                                colors = ButtonDefaults.textButtonColors(contentColor = DreamColors.Danger)
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Outlined.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Remove")
                                }
                            }
                        }
                    }

                    // Image Preview Box
                    imageUri?.let { uri ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, DreamColors.InkFaint.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = uri,
                                contentDescription = "Attached screenshot",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }

                    // Diagnostics Checkbox
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = !isSubmitting && isConfigValid) {
                                includeDiagnostics = !includeDiagnostics
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = includeDiagnostics,
                            onCheckedChange = { includeDiagnostics = it },
                            enabled = !isSubmitting && isConfigValid,
                            colors = CheckboxDefaults.colors(
                                checkedColor = DreamColors.AuroraSoft,
                                uncheckedColor = DreamColors.InkMuted,
                                checkmarkColor = DreamColors.Night
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = "Include phone/app diagnostics",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DreamColors.Ink
                            )
                            Text(
                                text = "Includes app version, Android version, brand, available memory/disk storage.",
                                style = MaterialTheme.typography.bodySmall,
                                color = DreamColors.InkMuted
                            )
                        }
                    }
                }

                HorizontalDivider(color = DreamColors.InkFaint.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

                // Footer Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isSubmitting,
                        colors = ButtonDefaults.textButtonColors(contentColor = DreamColors.InkMuted)
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))

                    val canSubmit = title.isNotBlank() && description.isNotBlank() && isConfigValid && !isSubmitting

                    Button(
                        onClick = {
                            viewModel.submitReport(
                                title = title,
                                description = description,
                                includeDiagnostics = includeDiagnostics,
                                name = name,
                                email = email,
                                imageUri = imageUri
                            )
                        },
                        enabled = canSubmit,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DreamColors.AuroraSoft,
                            contentColor = DreamColors.Ink,
                            disabledContainerColor = DreamColors.IndigoSoft.copy(alpha = 0.3f),
                            disabledContentColor = DreamColors.InkMuted.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSubmitting) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = DreamColors.Ink)
                                Text("Submitting...")
                            }
                        } else {
                            Text("Submit Report")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IssueDetailsDialog(
    report: BugReport,
    viewModel: FeedbackViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val details by viewModel.selectedReportDetails.collectAsState()
    val comments by viewModel.selectedReportComments.collectAsState()
    val isLoading by viewModel.isLoadingDetails.collectAsState()
    val error by viewModel.detailsError.collectAsState()

    val isPostingComment by viewModel.isPostingComment.collectAsState()
    val commentError by viewModel.commentError.collectAsState()

    var replyText by remember { mutableStateOf("") }
    var replyImageUri by remember { mutableStateOf<Uri?>(null) }

    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> replyImageUri = uri }
    )

    LaunchedEffect(report) {
        viewModel.loadIssueDetails(report)
    }

    // Reset comment field on comments refresh
    LaunchedEffect(comments) {
        replyText = ""
        replyImageUri = null
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(20.dp),
            color = DreamColors.NightDeep,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(DreamSpacing.md)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Report Details",
                            style = MaterialTheme.typography.titleLarge,
                            color = DreamColors.Moonglow,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Issue #${report.number}",
                            style = MaterialTheme.typography.bodySmall,
                            color = DreamColors.InkMuted
                        )
                    }

                    Row {
                        IconButton(
                            onClick = { viewModel.loadIssueDetails(report) },
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Refresh", tint = DreamColors.InkMuted)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = DreamColors.InkMuted)
                        }
                    }
                }

                HorizontalDivider(color = DreamColors.InkFaint.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

                if (isLoading && details == null) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = DreamColors.Moonglow)
                    }
                } else if (error != null && details == null) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = error ?: "Connection error",
                            color = DreamColors.Danger,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadIssueDetails(report) },
                            colors = ButtonDefaults.buttonColors(containerColor = DreamColors.IndigoSoft)
                        ) {
                            Text("Retry Connection")
                        }
                    }
                } else {
                    // Conversation and details list
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Original Issue Title & State Card
                        item {
                            details?.let { issue ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = DreamColors.IndigoSoft.copy(alpha = 0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val badgeColor = if (issue.state.equals("open", ignoreCase = true)) {
                                                DreamColors.Success
                                            } else {
                                                DreamColors.Danger
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .border(1.dp, badgeColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                                    .background(badgeColor.copy(alpha = 0.1f))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = issue.state.uppercase(),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = badgeColor
                                                )
                                            }

                                            IconButton(
                                                onClick = {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(issue.html_url))
                                                    context.startActivity(intent)
                                                }
                                            ) {
                                                Icon(Icons.Outlined.Launch, contentDescription = "Open in browser", tint = DreamColors.Moonglow)
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = issue.title.replace("[Feedback] ", ""),
                                            style = MaterialTheme.typography.titleMedium,
                                            color = DreamColors.Ink,
                                            fontWeight = FontWeight.Bold
                                        )

                                        issue.body?.let { originalBody ->
                                            Spacer(modifier = Modifier.height(8.dp))
                                            HorizontalDivider(color = DreamColors.InkFaint.copy(alpha = 0.15f))
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = parseMarkdownForUi(originalBody),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = DreamColors.InkMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Comments Heading
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            ) {
                                Icon(Icons.Outlined.Comment, contentDescription = null, tint = DreamColors.Moonglow, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Replies",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = DreamColors.Moonglow,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (comments.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No replies yet.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = DreamColors.InkFaint
                                    )
                                }
                            }
                        } else {
                            items(comments) { comment ->
                                CommentBubble(comment)
                            }
                        }
                    }
                }

                HorizontalDivider(color = DreamColors.InkFaint.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))

                // Comment Posting Area
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    commentError?.let { err ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DreamColors.Danger.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        ) {
                            Text(text = err, style = MaterialTheme.typography.bodySmall, color = DreamColors.Danger)
                        }
                    }

                    // Optional Image selected preview in Reply box
                    replyImageUri?.let { uri ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DreamColors.IndigoSoft.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                .padding(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Selected reply attachment",
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Text(
                                    text = "Screenshot attachment loaded",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DreamColors.InkMuted
                                )
                            }

                            IconButton(onClick = { replyImageUri = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear image", tint = DreamColors.Danger, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    // Input & Send button Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            enabled = !isPostingComment
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CloudUpload,
                                contentDescription = "Add Screenshot",
                                tint = if (replyImageUri != null) DreamColors.AuroraSoft else DreamColors.InkMuted
                            )
                        }

                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            placeholder = { Text("Write a reply...") },
                            modifier = Modifier.weight(1f),
                            maxLines = 3,
                            enabled = !isPostingComment,
                            colors = getTextFieldColors()
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        val canSend = replyText.isNotBlank() && !isPostingComment
                        Button(
                            onClick = {
                                viewModel.postComment(report, replyText, replyImageUri)
                            },
                            enabled = canSend,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DreamColors.AuroraSoft,
                                contentColor = DreamColors.Ink,
                                disabledContainerColor = DreamColors.IndigoSoft.copy(alpha = 0.3f),
                                disabledContentColor = DreamColors.InkMuted.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            if (isPostingComment) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = DreamColors.Ink)
                            } else {
                                Text("Send")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommentBubble(comment: com.charles.app.dreamloom.data.feedback.GithubComment) {
    val isDeveloper = comment.user.login.equals(com.charles.app.dreamloom.BuildConfig.GITHUB_REPO_OWNER, ignoreCase = true)
    
    val bubbleColor = if (isDeveloper) {
        DreamColors.AuroraSoft.copy(alpha = 0.15f)
    } else {
        DreamColors.IndigoSoft.copy(alpha = 0.15f)
    }
    
    val borderCol = if (isDeveloper) {
        DreamColors.AuroraSoft.copy(alpha = 0.3f)
    } else {
        DreamColors.IndigoSoft.copy(alpha = 0.3f)
    }

    val nameLabel = if (isDeveloper) {
        "${comment.user.login} (Developer)"
    } else {
        comment.user.login
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderCol, RoundedCornerShape(12.dp))
            .background(bubbleColor, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = nameLabel,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = if (isDeveloper) DreamColors.Moonglow else DreamColors.Cyanwash
            )
            Text(
                text = formatDate(comment.created_at),
                style = MaterialTheme.typography.labelSmall,
                color = DreamColors.InkFaint
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = parseMarkdownForUi(comment.body),
            style = MaterialTheme.typography.bodyMedium,
            color = DreamColors.Ink
        )
    }
}

@Composable
fun getTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = DreamColors.Ink,
    unfocusedTextColor = DreamColors.Ink,
    focusedContainerColor = DreamColors.IndigoSoft.copy(alpha = 0.2f),
    unfocusedContainerColor = DreamColors.IndigoSoft.copy(alpha = 0.1f),
    disabledContainerColor = DreamColors.IndigoSoft.copy(alpha = 0.05f),
    focusedBorderColor = DreamColors.Moonglow,
    unfocusedBorderColor = DreamColors.InkFaint,
    focusedLabelColor = DreamColors.Moonglow,
    unfocusedLabelColor = DreamColors.InkMuted,
    focusedPlaceholderColor = DreamColors.InkFaint,
    unfocusedPlaceholderColor = DreamColors.InkFaint
)

private fun formatDate(dateStr: String): String {
    return try {
        // Safe standard parser for "YYYY-MM-DD"
        val dateOnly = dateStr.substringBefore("T")
        val parts = dateOnly.split("-")
        if (parts.size == 3) {
            "${parts[1]}/${parts[2]}/${parts[0]}"
        } else {
            dateStr
        }
    } catch (e: Exception) {
        dateStr
    }
}

private fun parseMarkdownForUi(markdownBody: String): String {
    // Strip markdown formatting simple style for Android Text views
    return markdownBody
        .replace(Regex("## Description\\s+"), "")
        .replace(Regex("## Contact Info\\s+"), "")
        .replace(Regex("## Reply\\s+"), "")
        .replace(Regex("## Attachment\\s+"), "")
        .replace(Regex("## Diagnostics\\s+"), "DEVICE INFO:\n")
        .replace(Regex("!\\[Screenshot\\]\\(([^)]+)\\)"), "Attached Screenshot: $1")
        .replace(Regex("\\*\\*"), "")
        .trim()
}
