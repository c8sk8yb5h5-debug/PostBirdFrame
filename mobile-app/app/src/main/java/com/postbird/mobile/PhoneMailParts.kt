package com.postbird.mobile

import java.io.File
import javax.mail.Multipart
import javax.mail.Part
import javax.mail.internet.MimeUtility

object PhoneMailParts {
    fun text(content: Any?): String {
        return when (content) {
            is String -> content
            is Multipart -> {
                val builder = StringBuilder()
                for (i in 0 until content.count) {
                    builder.append(text(content.getBodyPart(i).content)).append('\n')
                }
                builder.toString()
            }
            else -> ""
        }
    }

    fun saveFirstUpdateAttachment(part: Part, dir: File, fallbackName: String): File? {
        if (part.isMimeType("multipart/*")) {
            val content = part.content
            if (content is Multipart) {
                var fallback: File? = null
                for (index in 0 until content.count) {
                    val found = saveFirstUpdateAttachment(content.getBodyPart(index), dir, fallbackName)
                    if (found != null && found.name.contains(PhoneUpdateRules.ATTACHMENT_KEY, ignoreCase = true)) return found
                    if (fallback == null) fallback = found
                }
                return fallback
            }
            return null
        }

        val fileName = normalizedFileName(part)
        val canSave = isPreferredPhoneUpdateAttachment(fileName) || isSupportedUpdateAttachment(fileName) || isUnnamedAttachment(part)
        if (!canSave) return null

        if (!dir.exists()) dir.mkdirs()
        val saveName = when {
            fileName.isNotBlank() && isSupportedUpdateAttachment(fileName) -> fileName
            else -> fallbackName
        }
        val outputFile = File(dir, saveName)
        if (outputFile.exists()) outputFile.delete()
        part.inputStream.use { input ->
            outputFile.outputStream().use { output ->
                val buffer = ByteArray(65536)
                while (true) {
                    val read = input.read(buffer)
                    if (read < 0) break
                    output.write(buffer, 0, read)
                }
                output.flush()
            }
        }
        return outputFile
    }

    private fun normalizedFileName(part: Part): String {
        val raw = part.fileName.orEmpty()
        val decoded = try { MimeUtility.decodeText(raw) } catch (_: Exception) { raw }
        return decoded.substringAfterLast('/').substringAfterLast('\\').replace(" ", "").trim()
    }

    private fun isPreferredPhoneUpdateAttachment(fileName: String): Boolean {
        return fileName.contains(PhoneUpdateRules.ATTACHMENT_KEY, ignoreCase = true) && isSupportedUpdateAttachment(fileName)
    }

    private fun isSupportedUpdateAttachment(fileName: String): Boolean {
        return fileName.endsWith(".apk", ignoreCase = true) ||
            fileName.endsWith(".apk.zip", ignoreCase = true) ||
            fileName.endsWith(".apk.bin", ignoreCase = true) ||
            fileName.endsWith(".zip", ignoreCase = true)
    }

    private fun isUnnamedAttachment(part: Part): Boolean {
        return Part.ATTACHMENT.equals(part.disposition, true) && part.fileName.isNullOrBlank()
    }
}
