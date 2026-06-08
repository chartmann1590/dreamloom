package com.charles.app.dreamloom.data.feedback

import kotlinx.serialization.Serializable

@Serializable
data class BugReport(
    val number: Int,
    val title: String,
    val status: String,
    val createdAt: String,
    val htmlUrl: String
)

@Serializable
data class CreateIssueRequest(
    val title: String,
    val body: String
)

@Serializable
data class GithubIssue(
    val number: Int,
    val title: String,
    val state: String,
    val html_url: String,
    val created_at: String,
    val body: String? = null
)

@Serializable
data class GithubUser(
    val login: String
)

@Serializable
data class GithubComment(
    val id: Long,
    val body: String,
    val created_at: String,
    val user: GithubUser
)

@Serializable
data class PostCommentRequest(
    val body: String
)

@Serializable
data class UploadAssetRequest(
    val message: String,
    val content: String
)

@Serializable
data class UploadContentInfo(
    val download_url: String? = null,
    val html_url: String? = null
)

@Serializable
data class UploadAssetResponse(
    val content: UploadContentInfo? = null
)
