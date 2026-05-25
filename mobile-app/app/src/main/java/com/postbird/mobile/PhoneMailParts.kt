package com.postbird.mobile

import java.io.File
import javax.mail.Multipart
import javax.mail.Part

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

    fun saveFirstUpdateAttachment(part: Part, dir: File): File? {
        if (part.isMimeType("multipart/*")) {
            val content = part.content
            if (content is Multipart) {
                for (index in 0 until content.count) {
                    val found = saveFirstUpdateAttachment(content.getBodyPart(index), dir)
                    if (found != null) return found
                }
            }
            return null
        }

        val fileName = part.fileName.orEmpty().substringAfterLast('/')
        if (!isPhoneUpdateAttachment(fileName)) return null
        if (!dir.exists()) dir.mkdirs()
        val outputFile = File(dir, fileName)
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

    private fun isPhoneUpdateAttachment(fileName: String): Boolean {
        val supportedSuffix = fileName.endsWith(".apk", ignoreCase = true) ||
            fileName.endsWith(".apk.zip", ignoreCase = true) ||
            fileName.endsWith(".apk.bin", ignoreCase = true) ||
            fileName.endsWith(".zip", ignoreCase = true)
        return fileName.contains(PhoneUpdateRules.ATTACHMENT_KEY, ignoreCase = true) && supportedSuffix
    }
}
