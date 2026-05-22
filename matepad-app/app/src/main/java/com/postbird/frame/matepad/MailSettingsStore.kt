package com.postbird.frame.matepad.settings

import android.content.Context

class MailSettingsStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        STORE_NAME,
        Context.MODE_PRIVATE
    )

    fun load(): MailSettings {
        return MailSettings(
            email = preferences.getString(KEY_EMAIL, "").orEmpty(),
            authCode = preferences.getString(KEY_AUTH_CODE, "").orEmpty(),
            autoCheckEnabled = preferences.getBoolean(KEY_AUTO_CHECK_ENABLED, true),
            lastSavedAt = preferences.getLong(KEY_LAST_SAVED_AT, 0L)
        )
    }

    fun save(settings: MailSettings) {
        preferences.edit()
            .putString(KEY_EMAIL, settings.email)
            .putString(KEY_AUTH_CODE, settings.authCode)
            .putBoolean(KEY_AUTO_CHECK_ENABLED, settings.autoCheckEnabled)
            .putLong(KEY_LAST_SAVED_AT, settings.lastSavedAt)
            .apply()
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    companion object {
        private const val STORE_NAME = "postbird_mail_settings"
        private const val KEY_EMAIL = "email"
        private const val KEY_AUTH_CODE = "auth_code"
        private const val KEY_AUTO_CHECK_ENABLED = "auto_check_enabled"
        private const val KEY_LAST_SAVED_AT = "last_saved_at"
    }
}
