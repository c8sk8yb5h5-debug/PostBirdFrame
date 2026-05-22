package com.postbird.frame.matepad.settings

data class MailSettings(
    val email: String = "",
    val authCode: String = "",
    val autoCheckEnabled: Boolean = true,
    val lastSavedAt: Long = 0L
) {
    val hasMailConfig: Boolean
        get() = email.isNotBlank() && authCode.isNotBlank()
}
