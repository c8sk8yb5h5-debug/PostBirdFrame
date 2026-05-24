package com.postbird.frame.matepad.mail

import android.content.Context
import android.os.Environment
import java.io.File

class MediaReceiveStore(private val context: Context) {
    fun mediaDirectory(): File {
        val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), DIR_NAME)
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun listMediaFiles(): List<File> {
        val dir = mediaDirectory()
        return dir.listFiles()
            ?.filter { it.isFile && isSupportedMedia(it.name) }
            ?.sortedByDescending { it.lastModified() }
            .orEmpty()
    }

    fun createTargetFile(fileName: String): File {
        val dir = mediaDirectory()
        val safeName = fileName.substringAfterLast('/').substringAfterLast('\\').ifBlank { "postbird_media" }
        val base = safeName.substringBeforeLast('.', safeName)
        val ext = safeName.substringAfterLast('.', "")
        var target = File(dir, safeName)
        var index = 1
        while (target.exists()) {
            val name = if (ext.isBlank()) "${base}_$index" else "${base}_$index.$ext"
            target = File(dir, name)
            index++
        }
        return target
    }

    fun isSupportedMedia(fileName: String): Boolean {
        val lower = fileName.lowercase()
        if (lower.contains("postbird-matepad") || lower.contains("postbird-phone")) return false
        if (lower.endsWith(".apk") || lower.endsWith(".apk.bin") || lower.endsWith(".apk.zip")) return false
        return SUPPORTED_SUFFIXES.any { lower.endsWith(it) }
    }

    companion object {
        private const val DIR_NAME = "postbird_media"
        private val SUPPORTED_SUFFIXES = listOf(
            ".jpg", ".jpeg", ".png", ".webp", ".gif",
            ".mp4", ".mov", ".m4v", ".3gp"
        )
    }
}
