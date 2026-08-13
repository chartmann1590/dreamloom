package com.charles.app.dreamloom.data.feedback

import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Talks to the cloudflare-worker/ feedback relay, not api.github.com directly. See
 * cloudflare-worker/src/index.ts, which holds the GitHub token server-side as a Worker
 * secret. Previously this embedded BuildConfig.GITHUB_API_TOKEN client-side as a Bearer
 * header, which shipped a real repo-write PAT in every release build (extractable from
 * the APK).
 */
@Singleton
class GithubApi @Inject constructor(
    baseOkHttpClient: OkHttpClient
) {
    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    private val client: OkHttpClient = baseOkHttpClient.newBuilder()
        .addInterceptor { chain ->
            val request = chain.request()
            Log.d("GithubApi", "Sending request to ${request.url}")
            val response = chain.proceed(request)
            Log.d("GithubApi", "Received response code ${response.code} from ${response.request.url}")
            response
        }
        .build()

    private val baseUrl = "https://dreamloom-github-feedback.charles-h-hartmann1.workers.dev"

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    @Throws(IOException::class)
    fun createIssue(request: CreateIssueRequest): GithubIssue {
        val body = json.encodeToString(request).toRequestBody(jsonMediaType)
        val httpRequest = Request.Builder().url("$baseUrl/issue").post(body).build()

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
    fun getIssue(number: Int): GithubIssue {
        val httpRequest = Request.Builder().url("$baseUrl/issue/$number").get().build()

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
    fun getComments(number: Int): List<GithubComment> {
        val httpRequest = Request.Builder().url("$baseUrl/issue/$number/comments").get().build()

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
    fun postComment(number: Int, request: PostCommentRequest): GithubComment {
        val body = json.encodeToString(request).toRequestBody(jsonMediaType)
        val httpRequest = Request.Builder().url("$baseUrl/issue/$number/comments").post(body).build()

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
    fun uploadAsset(request: UploadAssetRequest): UploadAssetResponse {
        val body = json.encodeToString(request).toRequestBody(jsonMediaType)
        val httpRequest = Request.Builder().url("$baseUrl/upload-image").post(body).build()

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
