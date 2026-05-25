package com.postbird.mobile

import java.io.File
import javax.mail.Folder

object PhoneUpdateFinder {
    fun find(folder: Folder, cacheDir: File, currentVersionCode: Int): File {
        val messages = PhoneMessageWindow.latest(folder, PhoneBoxConfig.SCAN_LIMIT)
        var matchedSubjectCount = 0
        var parsedInfoCount = 0
        val recentSubjects = mutableListOf<String>()

        for (message in messages) {
            val subject = message.subject ?: ""
            if (recentSubjects.size < 5) recentSubjects.add(subject)
            if (!subject.contains(PhoneUpdateRules.SUBJECT_KEY)) continue
            matchedSubjectCount += 1

            val infoFromSubject = PhoneSubjectUpdateParser.parse(subject)
            val info = infoFromSubject ?: PhoneUpdateParser.parse(PhoneMailParts.text(message.content)) ?: continue
            parsedInfoCount += 1

            if (info.versionCode <= currentVersionCode) continue

            val dir = File(cacheDir, "updates")
            val binFile = PhoneMailParts.saveFirstUpdateAttachment(message, dir)
                ?: throw IllegalStateException("已命中更新邮件，但未找到 ${PhoneUpdateRules.ATTACHMENT_KEY} 更新附件")
            val apkFile = File(dir, info.originalApkName)
            if (apkFile.exists()) apkFile.delete()
            binFile.copyTo(apkFile, overwrite = true)
            if (info.sha256.isNotBlank()) {
                val actual = PhoneHash.sha256(apkFile)
                if (!actual.equals(info.sha256, ignoreCase = true)) {
                    apkFile.delete()
                    throw IllegalStateException("更新包校验失败")
                }
            }
            return apkFile
        }

        val titles = recentSubjects.joinToString(" | ") { it.take(40) }
        throw IllegalStateException("未发现手机端可用更新包。当前code=${currentVersionCode}，扫描=${messages.size}，标题命中=${matchedSubjectCount}，解析=${parsedInfoCount}。最近标题：${titles}")
    }
}
