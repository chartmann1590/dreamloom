package com.charles.app.dreamloom.llm.backend

import android.os.Build

object QnnDetector {
    fun hasHexagon(): Boolean {
        val m = Build.SOC_MANUFACTURER ?: ""
        val model = Build.SOC_MODEL ?: ""
        return m.contains("Qualcomm", ignoreCase = true) &&
            model.matches(Regex("SM\\d{4}"))
    }
}
