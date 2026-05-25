package com.postbird.mobile

object PhoneSubjectUpdateParser {
    fun parse(subject: String): PhoneUpdateInfo? {
        if (!subject.contains(PhoneUpdateRules.SUBJECT_KEY)) return null
        val version = Regex("v([^\\s]+)").find(subject)?.groupValues?.getOrNull(1) ?: return null
        val code = Regex("code=([0-9]+)").find(subject)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: return null
        val binName = "PostBird-Phone-v${version}-code${code}.apk.bin"
        val apkName = binName.removeSuffix(".bin")
        return PhoneUpdateInfo(
            versionName = version,
            versionCode = code,
            attachmentName = binName,
            originalApkName = apkName,
            sha256 = ""
        )
    }
}
