package com.charles.app.dreamloom.data.feedback

import android.util.Log
import com.charles.app.dreamloom.BuildConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GithubApi @Inject constructor(
    baseOkHttpClient: OkHttpClient
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val client: OkHttpClient

    init {
        val builder = baseOkHttpClient.newBuilder()

        // Redacting custom logging interceptor to satisfy security requirements
        builder.addInterceptor { chain ->
            val request = chain.request()
            val redactedHeaders = request.headers.toString()
                .replace(Regex("Authorization: Bearer \\S+", RegexOption.IGNORE_CASE), "Authorization: Bearer [REDACTED]")
            Log.d("GithubApi", "Sending request to ${request.url}\nHeaders:\n$redactedHeaders")

            val response = chain.proceed(request)
            Log.d("GithubApi", "Received response code ${response.code} from ${response.request.url}")
            response
        }

        // Interceptor to add global headers automatically
        builder.addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Accept", "application/vnd.github+json")
                .header("X-GitHub-Api-Version", "2022-11-28")
                .header("User-Agent", "Dreamloom-Android/0.1")

            val token = BuildConfig.GITHUB_API_TOKEN
            if (token.isNotEmpty()) {
                requestBuilder.header("Authorization", "Bearer $token")
            }
            chain.proceed(requestBuilder.build())
        }

        client = builder.build()
    }

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    @Throws(IOException::class)
    fun createIssue(owner: String, repo: String, request: CreateIssueRequest): GithubIssue {
        val url = "https://api.github.com/repos/$owner/$repo/issues"
        val body = json.encodeToString(request).toRequestBody(jsonMediaType)
        val httpRequest = Request.Builder().url(url).post(body).build()

        client.newCall(httpRequest).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                throw IOException("Failed to create issue (${response.code}): $errorBody")
            }
            val responseBody = response.body?.string() ?: throw IOException("Empty response body")
            return json.decodeFromString(responseBody)
        }
    }

    @Throws(IOException::class)
    fun getIssue(owner: String, repo: String, number: Int): GithubIssue {
        val url = "https://api.github.com/repos/$owner/$repo/issues/$number"
        val httpRequest = Request.Builder().url(url).get().build()

        client.newCall(httpRequest).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                throw IOException("Failed to get issue (${response.code}): $errorBody")
            }
            val responseBody = response.body?.string() ?: throw IOException("Empty response body")
            return json.decodeFromString(responseBody)
        }
    }

    @Throws(IOException::class)
    fun getComments(owner: String, repo: String, number: Int): List<GithubComment> {
        val url = "https://api.github.com/repos/$owner/$repo/issues/$number/comments"
        val httpRequest = Request.Builder().url(url).get().build()

        client.newCall(httpRequest).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                throw IOException("Failed to get comments (${response.code}): $errorBody")
            }
            val responseBody = response.body?.string() ?: throw IOException("Empty response body")
            return json.decodeFromString(responseBody)
        }
    }

    @Throws(IOException::class)
    fun postComment(owner: String, repo: String, number: Int, request: PostCommentRequest): GithubComment {
        val url = "https://api.github.com/repos/$owner/$repo/issues/$number/comments"
        val body = json.encodeToString(request).toRequestBody(jsonMediaType)
        val httpRequest = Request.Builder().url(url).post(body).build()

        client.newCall(httpRequest).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                throw IOException("Failed to post comment (${response.code}): $errorBody")
            }
            val responseBody = response.body?.string() ?: throw IOException("Empty response body")
            return json.decodeFromString(responseBody)
        }
    }

    @Throws(IOException::class)
    fun uploadAsset(owner: String, repo: String, assetDir: String, filename: String, request: UploadAssetRequest): UploadAssetResponse {
        val url = "https://api.github.com/repos/$owner/$repo/contents/$assetDir/$filename"
        val body = json.encodeToString(request).toRequestBody(jsonMediaType)
        val httpRequest = Request.Builder().url(url).put(body).build()

        client.newCall(httpRequest).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: ""
                throw IOException("Failed to upload asset (${response.code}): $errorBody")
            }
            val responseBody = response.body?.string() ?: throw IOException("Empty response body")
            return json.decodeFromString(responseBody)
        }
    }
}
