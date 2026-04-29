package com.charles.app.dreamloom.navigation

object Routes {
    const val SPLASH = "splash"
    const val WELCOME = "onboarding/welcome"
    const val PRIVACY = "onboarding/privacy"
    const val MODEL_DOWNLOAD = "onboarding/modelDownload"
    const val PERMISSIONS = "onboarding/permissions"
    const val ONBOARDING_NOTIFICATIONS = "onboarding/notifications"
    const val HOME = "home"
    const val RECORDING = "newDream/recording"
    const val INTERPRETING = "newDream/interpreting/{id}"
    fun interpreting(id: Long) = "newDream/interpreting/$id"
    const val DREAM_DETAIL = "dreamDetail/{id}"
    fun dreamDetail(id: Long) = "dreamDetail/$id"
    const val ATLAS = "atlas"
    const val INSIGHT = "insight"
    const val ORACLE = "oracle"
    const val SETTINGS = "settings/root"
    const val SETTINGS_PRIVACY = "settings/privacy"
    const val SETTINGS_REMINDERS = "settings/reminders"
    const val SETTINGS_ABOUT = "settings/about"
}
