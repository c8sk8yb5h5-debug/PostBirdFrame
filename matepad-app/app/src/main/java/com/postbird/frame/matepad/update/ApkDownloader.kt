package com.postbird.frame.matepad.update

import android.app.DownloadManager
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import java.io.File

class ApkDownloader {
    fun download(context: Context, url: String, fileName: String = "PostBird-MatePad-update.apk"): File {
        val updatesDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "updates")
        if (!updatesDir.exists()) updatesDir.mkdirs()

        val outputFile = File(updatesDir, fileName)
        if (outputFile.exists()) outputFile.delete()

        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("邮差鸟相框更新包")
            .setDescription("正在下载最新 APK")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)
            .setDestinationUri(Uri.fromFile(outputFile))

        val manager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = manager.enqueue(request)
        waitForDownload(manager, downloadId)

        if (!outputFile.exists() || outputFile.length() <= 0L) {
            throw IllegalStateException("下载文件为空")
        }

        return outputFile
    }

    private fun waitForDownload(manager: DownloadManager, downloadId: Long) {
        val startedAt = System.currentTimeMillis()
        while (true) {
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = manager.query(query)
            cursor.use {
                if (it != null && it.moveToFirst()) {
                    val status = it.getIntValue(DownloadManager.COLUMN_STATUS)
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> return
                        DownloadManager.STATUS_FAILED -> {
                            val reason = it.getIntValue(DownloadManager.COLUMN_REASON)
                            throw IllegalStateException("系统下载失败：$reason")
                        }
                    }
                }
            }

            if (System.currentTimeMillis() - startedAt > DOWNLOAD_TIMEOUT_MS) {
                manager.remove(downloadId)
                throw IllegalStateException("系统下载超时")
            }

            Thread.sleep(POLL_INTERVAL_MS)
        }
    }

    private fun Cursor.getIntValue(columnName: String): Int {
        val index = getColumnIndex(columnName)
        return if (index >= 0) getInt(index) else -1
    }

    companion object {
        private const val DOWNLOAD_TIMEOUT_MS = 180000L
        private const val POLL_INTERVAL_MS = 800L
    }
}
