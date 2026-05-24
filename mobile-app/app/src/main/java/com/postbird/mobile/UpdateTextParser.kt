package com.postbird.mobile

object UpdateTextParser {
    fun parse(text: String): UpdateInfo? {
        val data = mutableMapOf<String, String>()
        text.lines().forEach { line ->
            val index = line.indexOf('=')
            if (index > 0) data[line.substring(0, index).trim()] = line.substring(index + 1).trim()
        }
        val versionCode = data["versionCode"]?.toIntOrNull() ?: return null
        return UpdateInfo(
            versionName = data["versionName"] ?: "",
            versionCode = versionCode,
            attachmentName = data["attachmentName"] ?: "",
            originalApkName = data["originalApkName"] ?: "postbird-mobile-update.apk",
            sha256 = data["sha256"] ?: ""
        )
    }
}
