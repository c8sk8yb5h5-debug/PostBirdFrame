package com.postbird.mobile

import android.content.Context

data class MobileSettings(
    val email: String = "",
    val authCode: String = "",
    val receiver: String = ""
) {
    val hasMailConfig: Boolean get() = email.isNotBlank() && authCode.isNotBlank()
}

class MobileSettingsStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("postbird_mobile_settings", Context.MODE_PRIVATE)

    fun load(): MobileSettings {
        return MobileSettings(
            email = prefs.getString("mail", "").orEmpty(),
            authCode = prefs.getString("mail_code", "").orEmpty(),
            receiver = prefs.getString("receiver", "").orEmpty()
        )
    }

    fun save(settings: MobileSettings) {
        prefs.edit()
            .putString("mail", settings.email.trim())
            .putString("mail_code", settings.authCode)
            .putString("receiver", settings.receiver.trim())
            .apply()
    }
}
