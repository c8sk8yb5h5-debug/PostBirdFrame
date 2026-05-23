package com.postbird.frame.matepad.media

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object LocalMediaNaming {
    fun buildDedupKey(
        sourceMailFrom: String,
        sourceMessageNumber: Int,
        attachmentName: String
    ): String {
        return listOf(
            sourceMailFrom.trim().lowercase(),
            sourceMessageNumber.toString(),
            attachmentName.trim().lowercase()
        ).joinToString(separator = "|")
    }

    fun buildFileName(
        sourceMailFrom: String,
        sourceMessageNumber: Int,
        attachmentName: String,
        timestamp: Long = System.currentTimeMillis()
    ): String {
        val extension = attachmentName.substringAfterLast('.', missingDelimiterValue = "bin").lowercase()
        val timeText = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date(timestamp))
        val shortHash = sha256Short(buildDedupKey(sourceMailFrom, sourceMessageNumber, attachmentName))
        return "postbird_${timeText}_${shortHash}.${extension}"
    }

    fun buildId(
        sourceMailFrom: String,
        sourceMessageNumber: Int,
        attachmentName: String
    ): String {
        return sha256Short(buildDedupKey(sourceMailFrom, sourceMessageNumber, attachmentName), length = 16)
    }

    private fun sha256Short(value: String, length: Int = 8): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(value.toByteArray(Charsets.UTF_8))
            .joinToString(separator = "") { byte -> "%02x".format(byte) }
        return digest.take(length)
    }
}
