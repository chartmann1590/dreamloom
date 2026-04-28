package com.charles.app.dreamloom.feature.detail

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import com.charles.app.dreamloom.BuildConfig
import com.charles.app.dreamloom.R
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareDreamPng {
    fun openChooser(context: Context, dreamId: Long, bitmap: Bitmap) {
        val f = File(context.cacheDir, "dream_share_$dreamId.png")
        FileOutputStream(f).use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
        val uri = FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.fileprovider",
            f,
        )
        val i = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(i, context.getString(R.string.detail_share_chooser)),
        )
    }
}
