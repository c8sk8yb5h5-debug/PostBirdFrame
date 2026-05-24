package com.postbird.mobile

object PhoneUpdateParser {
    fun parse(text: String): PhoneUpdateInfo? {
        val data = mutableMapOf<String, String>()
        for (line in text.lines()) {
            val index = line.indexOf('=')
            if (index > 0) data[line.substring(0, index).trim()] = line.substring(index + 1).trim()
        }
        if (data["platform"] != PhoneUpdateRules.PLATFORM) return null
        if (data["applicationId"] != PhoneUpdateRules.APPLICATION_ID) return null
        val versionCode = data["versionCode"]?.toIntOrNull() ?: return null
        val attachmentName = data["attachmentName"] ?: return null
        if (!attachmentName.contains(PhoneUpdateRules.ATTACHMENT_KEY)) return null
        if (!attachmentName.endsWith(".apk.bin")) return null
        return PhoneUpdateInfo(
            versionName = data["versionName"] ?: "",
            versionCode = versionCode,
            attachmentName = attachmentName,
            originalApkName = data["originalApkName"] ?: attachmentName.removeSuffix(".bin"),
            sha256 = data["sha256"] ?: ""
        )
    }
}
