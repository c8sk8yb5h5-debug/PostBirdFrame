package com.postbird.frame.matepad.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class MailSettingsStore(context: Context) {
    private val encryptedPreferences: SharedPreferences?
    private val initializationError: Throwable?

    init {
        var prefs: SharedPreferences? = null
        var error: Throwable? = null

        try {
            val appContext = context.applicationContext
            val masterKey = MasterKey.Builder(appContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            prefs = EncryptedSharedPreferences.create(
                appContext,
                STORE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (throwable: Throwable) {
            error = throwable
        }

        encryptedPreferences = prefs
        initializationError = error
    }

    fun isSecureStorageReady(): Boolean {
        return encryptedPreferences != null
    }

    fun getStorageStatusText(): String {
        return if (encryptedPreferences != null) {
            "加密存储正常"
        } else {
            val reason = initializationError?.javaClass?.simpleName ?: "未知错误"
            "加密存储不可用：$reason"
        }
    }

    fun load(): MailSettings {
        val preferences = encryptedPreferences ?: return MailSettings()

        return MailSettings(
            email = preferences.getString(KEY_EMAIL, "").orEmpty(),
            authCode = preferences.getString(KEY_AUTH_CODE, "").orEmpty(),
            autoCheckEnabled = preferences.getBoolean(KEY_AUTO_CHECK_ENABLED, true),
            lastSavedAt = preferences.getLong(KEY_LAST_SAVED_AT, 0L)
        )
    }

    fun save(settings: MailSettings): Boolean {
        val preferences = encryptedPreferences ?: return false

        return preferences.edit()
            .putString(KEY_EMAIL, settings.email)
            .putString(KEY_AUTH_CODE, settings.authCode)
            .putBoolean(KEY_AUTO_CHECK_ENABLED, settings.autoCheckEnabled)
            .putLong(KEY_LAST_SAVED_AT, settings.lastSavedAt)
            .commit()
    }

    fun clear(): Boolean {
        val preferences = encryptedPreferences ?: return false
        return preferences.edit().clear().commit()
    }

    companion object {
        private const val STORE_NAME = "postbird_secure_mail_settings"
        private const val KEY_EMAIL = "email"
        private const val KEY_AUTH_CODE = "auth_code"
        private const val KEY_AUTO_CHECK_ENABLED = "auto_check_enabled"
        private const val KEY_LAST_SAVED_AT = "last_saved_at"
    }
}
