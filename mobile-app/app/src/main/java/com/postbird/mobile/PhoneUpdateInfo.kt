package com.postbird.mobile

data class PhoneUpdateInfo(
    val versionName: String,
    val versionCode: Int,
    val attachmentName: String,
    val originalApkName: String,
    val sha256: String
)
