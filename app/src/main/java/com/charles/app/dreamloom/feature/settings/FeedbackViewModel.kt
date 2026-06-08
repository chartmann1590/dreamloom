package com.charles.app.dreamloom.feature.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.app.dreamloom.BuildConfig
import com.charles.app.dreamloom.data.feedback.BugReport
import com.charles.app.dreamloom.data.feedback.BugReportRepo
import com.charles.app.dreamloom.data.feedback.CreateIssueRequest
import com.charles.app.dreamloom.data.feedback.DiagnosticsHelper
import com.charles.app.dreamloom.data.feedback.GithubApi
import com.charles.app.dreamloom.data.feedback.GithubComment
import com.charles.app.dreamloom.data.feedback.GithubIssue
import com.charles.app.dreamloom.data.feedback.ImageUploadHelper
import com.charles.app.dreamloom.data.feedback.PostCommentRequest
import com.charles.app.dreamloom.data.feedback.UploadAssetRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class FeedbackViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repo: BugReportRepo,
    private val api: GithubApi
) : ViewModel() {

    val bugReports: StateFlow<List<BugReport>> = repo.bugReports
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Creation State
    val isSubmitting = MutableStateFlow(false)
    val submitError = MutableStateFlow<String?>(null)
    val submitSuccess = MutableStateFlow(false)

    // Details/Comments State
    val selectedReportDetails = MutableStateFlow<GithubIssue?>(null)
    val selectedReportComments = MutableStateFlow<List<GithubComment>>(emptyList())
    val isLoadingDetails = MutableStateFlow(false)
    val detailsError = MutableStateFlow<String?>(null)

    // Comment State
    val isPostingComment = MutableStateFlow(false)
    val commentError = MutableStateFlow<String?>(null)

    fun hasValidConfig(): Boolean {
        return BuildConfig.GITHUB_API_TOKEN.isNotEmpty() &&
                BuildConfig.GITHUB_REPO_OWNER.isNotEmpty() &&
                BuildConfig.GITHUB_REPO_NAME.isNotEmpty()
    }

    fun submitReport(
        title: String,
        description: String,
        includeDiagnostics: Boolean,
        name: String,
        email: String,
        imageUri: Uri?
    ) {
        viewModelScope.launch {
            isSubmitting.value = true
            submitError.value = null
            submitSuccess.value = false

            try {
                if (!hasValidConfig()) {
                    throw Exception("Configuration error: GitHub repository info or token is missing.")
                }

                val owner = BuildConfig.GITHUB_REPO_OWNER
                val repoName = BuildConfig.GITHUB_REPO_NAME

                var attachmentUrl: String? = null
                if (imageUri != null) {
                    val base64 = withContext(Dispatchers.IO) {
                        ImageUploadHelper.uriToBase64(context, imageUri)
                    }
                    val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
                    val random = UUID.randomUUID().toString().take(6)
                    val filename = "issue-$timestamp-$random.png"

                    val uploadReq = UploadAssetRequest(
                        message = "Upload screenshot for bug report",
                        content = base64
                    )
                    val response = withContext(Dispatchers.IO) {
                        api.uploadAsset(owner, repoName, BuildConfig.FEEDBACK_ASSETS_DIR, filename, uploadReq)
                    }
                    attachmentUrl = response.content?.download_url ?: response.content?.html_url
                }

                val bodyBuilder = StringBuilder()
                bodyBuilder.append("## Description\n\n").append(description).append("\n\n")
                bodyBuilder.append("## Contact Info\n\n")
                bodyBuilder.append("- Name: ").append(name.ifBlank { "Not provided" }).append("\n")
                bodyBuilder.append("- Email: ").append(email.ifBlank { "Not provided" }).append("\n\n")

                if (attachmentUrl != null) {
                    bodyBuilder.append("## Attachment\n\n![Screenshot]($attachmentUrl)\n\n")
                }

                if (includeDiagnostics) {
                    val diagnostics = DiagnosticsHelper.getDiagnosticsMarkdown(context)
                    bodyBuilder.append(diagnostics).append("\n")
                }

                val issueReq = CreateIssueRequest(
                    title = "[Feedback] $title",
                    body = bodyBuilder.toString()
                )

                val issue = withContext(Dispatchers.IO) {
                    api.createIssue(owner, repoName, issueReq)
                }

                // Save to DataStore
                val localReport = BugReport(
                    number = issue.number,
                    title = issue.title.replace("[Feedback] ", ""),
                    status = issue.state,
                    createdAt = issue.created_at,
                    htmlUrl = issue.html_url
                )
                repo.saveBugReport(localReport)
                submitSuccess.value = true
            } catch (e: Exception) {
                submitError.value = e.message ?: "Unknown error occurred while submitting."
            } finally {
                isSubmitting.value = false
            }
        }
    }

    fun loadIssueDetails(report: BugReport) {
        viewModelScope.launch {
            isLoadingDetails.value = true
            detailsError.value = null
            selectedReportDetails.value = null
            selectedReportComments.value = emptyList()

            try {
                if (!hasValidConfig()) {
                    throw Exception("Configuration error: GitHub API values are not initialized.")
                }

                val owner = BuildConfig.GITHUB_REPO_OWNER
                val repoName = BuildConfig.GITHUB_REPO_NAME

                val issue = withContext(Dispatchers.IO) {
                    api.getIssue(owner, repoName, report.number)
                }
                selectedReportDetails.value = issue

                val comments = withContext(Dispatchers.IO) {
                    api.getComments(owner, repoName, report.number)
                }
                selectedReportComments.value = comments

                // Update local status if changed
                if (issue.state != report.status) {
                    val updatedReport = report.copy(status = issue.state)
                    repo.saveBugReport(updatedReport)
                }
            } catch (e: Exception) {
                detailsError.value = e.message ?: "Failed to load details from GitHub."
            } finally {
                isLoadingDetails.value = false
            }
        }
    }

    fun postComment(report: BugReport, bodyText: String, imageUri: Uri?) {
        viewModelScope.launch {
            isPostingComment.value = true
            commentError.value = null

            try {
                if (!hasValidConfig()) {
                    throw Exception("Configuration error: GitHub API values are not initialized.")
                }

                val owner = BuildConfig.GITHUB_REPO_OWNER
                val repoName = BuildConfig.GITHUB_REPO_NAME

                var attachmentUrl: String? = null
                if (imageUri != null) {
                    val base64 = withContext(Dispatchers.IO) {
                        ImageUploadHelper.uriToBase64(context, imageUri)
                    }
                    val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())
                    val random = UUID.randomUUID().toString().take(6)
                    val filename = "comment-${report.number}-$timestamp-$random.png"

                    val uploadReq = UploadAssetRequest(
                        message = "Upload comment attachment",
                        content = base64
                    )
                    val response = withContext(Dispatchers.IO) {
                        api.uploadAsset(owner, repoName, BuildConfig.FEEDBACK_ASSETS_DIR, filename, uploadReq)
                    }
                    attachmentUrl = response.content?.download_url ?: response.content?.html_url
                }

                val bodyBuilder = StringBuilder()
                bodyBuilder.append("## Reply\n\n").append(bodyText).append("\n\n")
                if (attachmentUrl != null) {
                    bodyBuilder.append("## Attachment\n\n![Screenshot]($attachmentUrl)\n\n")
                }

                val commentReq = PostCommentRequest(body = bodyBuilder.toString())
                withContext(Dispatchers.IO) {
                    api.postComment(owner, repoName, report.number, commentReq)
                }

                // Refresh comments and status
                loadIssueDetails(report)
            } catch (e: Exception) {
                commentError.value = e.message ?: "Failed to post comment."
            } finally {
                isPostingComment.value = false
            }
        }
    }

    fun resetSubmitState() {
        submitSuccess.value = false
        submitError.value = null
    }
}
