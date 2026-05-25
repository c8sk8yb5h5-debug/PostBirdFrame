package com.postbird.mobile

import java.io.File
import javax.mail.Folder

object PhoneUpdateFinder {
    fun find(folder: Folder, cacheDir: File): File {
        val messages = PhoneMessageWindow.latest(folder, PhoneBoxConfig.SCAN_LIMIT)
        for (message in messages) {
            val subject = message.subject ?: ""
            if (!subject.contains(PhoneUpdateRules.SUBJECT_KEY)) continue
            val text = PhoneMailParts.text(message.content)
            val info = PhoneUpdateParser.parse(text) ?: continue
            if (info.versionCode <= PhoneUpdateRules.CURRENT_VERSION_CODE) {
                throw IllegalStateException("当前已是最新版本")
            }
            val dir = File(cacheDir, "updates")
            val binFile = PhoneMailParts.saveAttachment(message.content, info.attachmentName, dir)
                ?: throw IllegalStateException("未找到手机端更新附件")
            val apkFile = File(dir, info.originalApkName)
            if (apkFile.exists()) apkFile.delete()
            if (!binFile.renameTo(apkFile)) throw IllegalStateException("更新包还原失败")
            if (info.sha256.isNotBlank()) {
                val actual = PhoneHash.sha256(apkFile)
                if (!actual.equals(info.sha256, ignoreCase = true)) {
                    apkFile.delete()
                    throw IllegalStateException("更新包校验失败")
                }
            }
            return apkFile
        }
        throw IllegalStateException("未发现手机端可用更新包")
    }
}
