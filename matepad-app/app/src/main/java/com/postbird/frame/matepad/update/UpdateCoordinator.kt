package com.postbird.frame.matepad.update

import android.content.Context
import com.postbird.frame.matepad.settings.MailSettingsStore
import java.io.File

class UpdateCoordinator {
    fun findUpdatePackage(context: Context): UpdatePackageResult {
        val currentCode = readCurrentVersionCode(context)
        val settings = MailSettingsStore(context).load()

        if (!settings.hasMailConfig) {
            return UpdatePackageResult(false, "请先在设置页保存 QQ 邮箱和授权码", null)
        }

        val mailResult = MailUpdatePackageDownloader().downloadLatestUpdate(
            context = context,
            email = settings.email,
            authCode = settings.authCode,
            currentVersionCode = currentCode
        )

        if (mailResult.success && mailResult.apkFile != null) {
            return UpdatePackageResult(true, mailResult.message, mailResult.apkFile)
        }

        return UpdatePackageResult(false, "QQ 邮箱未发现可用更新包：${mailResult.message}", null)
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
