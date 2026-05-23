package com.postbird.frame.matepad.update

import android.content.Context
import com.postbird.frame.matepad.settings.MailSettingsStore
import java.io.File

class UpdateCoordinator {
    fun findUpdatePackage(context: Context): UpdatePackageResult {
        val currentCode = readCurrentVersionCode(context)
        val settings = MailSettingsStore(context).load()

        if (settings.hasMailConfig) {
            val mailResult = MailUpdatePackageDownloader().downloadLatestUpdate(
                context = context,
                email = settings.email,
                authCode = settings.authCode,
                currentVersionCode = currentCode
            )
            if (mailResult.success && mailResult.apkFile != null) {
                return UpdatePackageResult(true, mailResult.message, mailResult.apkFile)
            }
        }

        val releaseInfo = GitHubReleaseClient().checkLatest(context)
        if (releaseInfo.apkDownloadUrl.isBlank()) {
            return UpdatePackageResult(false, releaseInfo.message, null)
        }

        return try {
            val file = ApkDownloader().download(context, releaseInfo.apkDownloadUrl)
            UpdatePackageResult(true, "已从 GitHub Release 下载更新包", file)
        } catch (error: Exception) {
            UpdatePackageResult(false, "GitHub 下载失败：${error.javaClass.simpleName}。可改用 QQ 邮箱发送更新包。", null)
        }
    }

    private fun readCurrentVersionCode(context: Context): Int {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode
            }
        } catch (_: Exception) {
            0
        }
    }
}

data class UpdatePackageResult(
    val success: Boolean,
    val message: String,
    val apkFile: File?
)
