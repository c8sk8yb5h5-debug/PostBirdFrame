package com.postbird.frame.matepad.update

data class UpdateInfo(
    val available: Boolean,
    val currentVersionName: String,
    val latestTagName: String,
    val apkName: String,
    val apkDownloadUrl: String,
    val message: String
)
