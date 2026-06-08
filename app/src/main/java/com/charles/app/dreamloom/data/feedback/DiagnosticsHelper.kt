package com.charles.app.dreamloom.data.feedback

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object DiagnosticsHelper {

    fun getDiagnosticsMarkdown(context: Context): String {
        val appName = "Dreamloom"
        val appPackage = context.packageName

        var versionName = "Unknown"
        var versionCode = 0L
        try {
            val packageInfo = context.packageManager.getPackageInfo(appPackage, 0)
            versionName = packageInfo.versionName ?: "Unknown"
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: Exception) {
            // Safe fallback
        }

        val deviceBrand = Build.BRAND
        val deviceModel = Build.MODEL
        val manufacturer = Build.MANUFACTURER
        val androidVersion = Build.VERSION.RELEASE
        val apiLevel = Build.VERSION.SDK_INT

        val locale = Locale.getDefault().toString()
        val timeZone = TimeZone.getDefault().id

        // Storage metrics
        var freeStorageGb = 0.0
        var totalStorageGb = 0.0
        try {
            val stat = StatFs(context.filesDir.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong
            freeStorageGb = (availableBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0)
            totalStorageGb = (totalBlocks * blockSize) / (1024.0 * 1024.0 * 1024.0)
        } catch (e: Exception) {
            // Safe fallback
        }

        // Memory metrics
        var freeMemGb = 0.0
        var totalMemGb = 0.0
        try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager.getMemoryInfo(memInfo)
            freeMemGb = memInfo.availMem / (1024.0 * 1024.0 * 1024.0)
            totalMemGb = memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
        } catch (e: Exception) {
            // Safe fallback
        }

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

        return """
            ## Diagnostics

            - App: $appName
            - Package: $appPackage
            - Version: $versionName ($versionCode)
            - Device: $deviceBrand $deviceModel
            - Manufacturer: $manufacturer
            - Android: $androidVersion / API $apiLevel
            - Locale: $locale
            - Time Zone: $timeZone
            - Storage Free/Total: ${String.format(Locale.US, "%.2f", freeStorageGb)} GB / ${String.format(Locale.US, "%.2f", totalStorageGb)} GB
            - Memory Free/Total: ${String.format(Locale.US, "%.2f", freeMemGb)} GB / ${String.format(Locale.US, "%.2f", totalMemGb)} GB
            - Timestamp: $timestamp
        """.trimIndent()
    }
}
