package com.postbird.frame.matepad.update

import android.content.Context
import android.os.Environment
import java.io.File
import java.util.zip.ZipInputStream

class MailUpdatePackageInstaller {
    fun prepareApk(context: Context, sourceFile: File): File {
        val updatesDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "mail_updates")
        if (!updatesDir.exists()) updatesDir.mkdirs()

        return when {
            sourceFile.name.endsWith(".apk", ignoreCase = true) -> sourceFile
            sourceFile.name.endsWith(".apk.bin", ignoreCase = true) -> copyAsApk(sourceFile, File(updatesDir, sourceFile.name.removeSuffix(".bin")))
            sourceFile.name.endsWith(".apk.zip", ignoreCase = true) -> unzipApk(sourceFile, updatesDir)
            sourceFile.name.endsWith(".zip", ignoreCase = true) -> unzipApk(sourceFile, updatesDir)
            else -> throw IllegalStateException("不支持的更新附件格式")
        }
    }

    private fun copyAsApk(sourceFile: File, targetFile: File): File {
        if (targetFile.exists()) targetFile.delete()
        sourceFile.inputStream().use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        validateApk(targetFile)
        return targetFile
    }

    private fun unzipApk(zipFile: File, targetDir: File): File {
        ZipInputStream(zipFile.inputStream()).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                val name = entry.name.substringAfterLast('/')
                if (!entry.isDirectory && name.endsWith(".apk", ignoreCase = true)) {
                    val outputFile = File(targetDir, name)
                    if (outputFile.exists()) outputFile.delete()
                    outputFile.outputStream().use { output ->
                        zip.copyTo(output)
                    }
                    validateApk(outputFile)
                    return outputFile
                }
                zip.closeEntry()
            }
        }
        throw IllegalStateException("压缩包内未找到 APK")
    }

    private fun validateApk(file: File) {
        if (!file.exists() || file.length() <= 0L) {
            throw IllegalStateException("APK 文件为空")
        }
    }
}
