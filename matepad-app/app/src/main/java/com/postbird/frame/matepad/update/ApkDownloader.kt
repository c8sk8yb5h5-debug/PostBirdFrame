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

        val connection = openConnectionWithRedirects(url)
        try {
            val code = connection.responseCode
            if (code !in 200..299) {
                val errorText = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                throw IllegalStateException("HTTP $code ${errorText.take(120)}")
            }

            connection.inputStream.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            if (!outputFile.exists() || outputFile.length() <= 0L) {
                throw IllegalStateException("下载文件为空")
            }

            return outputFile
        } finally {
            connection.disconnect()
        }
    }

    private fun openConnectionWithRedirects(startUrl: String): HttpURLConnection {
        var currentUrl = startUrl
        repeat(MAX_REDIRECTS) {
            val connection = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                instanceFollowRedirects = false
                requestMethod = "GET"
                connectTimeout = 30000
                readTimeout = 30000
                setRequestProperty("User-Agent", "PostBirdFrame-MatePad")
                setRequestProperty("Accept", "application/octet-stream")
            }

            val code = connection.responseCode
            if (code in 300..399) {
                val location = connection.getHeaderField("Location")
                connection.disconnect()
                if (location.isNullOrBlank()) {
                    throw IllegalStateException("下载重定向地址为空")
                }
                currentUrl = URL(URL(currentUrl), location).toString()
            } else {
                return connection
            }
        }

        throw IllegalStateException("下载重定向次数过多")
    }

    companion object {
        private const val MAX_REDIRECTS = 8
    }
}
