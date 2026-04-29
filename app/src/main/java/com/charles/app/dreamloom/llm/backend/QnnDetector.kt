package com.charles.app.dreamloom.llm.backend

import android.os.Build

object QnnDetector {
    fun hasHexagon(): Boolean {
        val m = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MANUFACTURER ?: ""
        } else {
            Build.MANUFACTURER ?: ""
        }
        val model = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MODEL ?: ""
        } else {
            Build.HARDWARE ?: ""
        }
        return m.contains("Qualcomm", ignoreCase = true) &&
            model.matches(Regex("SM\\d{4}"))
    }
}
