package com.charles.app.dreamloom.core.crypto

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.SecureRandom
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PassphraseProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
    }

    private val prefs: SharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "dreamloom_secrets",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    fun getOrCreateDbPassphrase(): ByteArray {
        val existing = prefs.getString(KEY_PASS, null)
        if (existing != null) {
            return android.util.Base64.decode(existing, android.util.Base64.DEFAULT)
        }
        val key = ByteArray(32)
        SecureRandom().nextBytes(key)
        val encoded = android.util.Base64.encodeToString(key, android.util.Base64.DEFAULT)
        prefs.edit().putString(KEY_PASS, encoded).apply()
        return key
    }

    fun clear() {
        prefs.edit().remove(KEY_PASS).apply()
    }

    companion object {
        private const val KEY_PASS = "room_pass"
    }
}
