package com.postbird.frame.matepad.update

import java.io.File

data class MailUpdateInfo(
    val success: Boolean,
    val message: String,
    val versionName: String = "",
    val versionCode: Int = 0,
    val attachmentName: String = "",
    val apkFile: File? = null
)
