package com.postbird.frame.matepad.update

import android.content.Context
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

class ApkDownloader {
    fun download(context: Context, url: String, fileName: String = "PostBird-MatePad-update.apk"): File {
        val updatesDir = File(context.cacheDir, "updates")
        if (!updatesDir.exists()) updatesDir.mkdirs()

        val outputFile = File(updatesDir, fileName)
        if (outputFile.exists()) outputFile.delete()

        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 30000
        connection.readTimeout = 30000
        connection.setRequestProperty("User-Agent", "PostBirdFrame-MatePad")

        try {
            val code = connection.responseCode
            if (code !in 200..299) {
                throw IllegalStateException("APK 下载失败：HTTP $code")
            }

            connection.inputStream.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } finally {
            connection.disconnect()
        }

        return outputFile
    }
}
