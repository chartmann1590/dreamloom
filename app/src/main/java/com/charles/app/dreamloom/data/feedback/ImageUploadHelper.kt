package com.charles.app.dreamloom.data.feedback

import android.content.Context
import android.net.Uri
import android.util.Base64
import java.io.IOException
import java.io.InputStream

object ImageUploadHelper {

    @Throws(IOException::class)
    fun uriToBase64(context: Context, uri: Uri): String {
        val inputStream: InputStream = context.contentResolver.openInputStream(uri)
            ?: throw IOException("Unable to open input stream for URI: $uri")
        return inputStream.use { stream ->
            val bytes = stream.readBytes()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        }
    }
}
