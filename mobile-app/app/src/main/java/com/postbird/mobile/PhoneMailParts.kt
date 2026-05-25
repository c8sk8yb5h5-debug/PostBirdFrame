package com.postbird.mobile

import java.io.File
import javax.mail.BodyPart
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

    fun saveAttachment(content: Any?, expectedName: String, dir: File): File? {
        if (content !is Multipart) return null
        if (!dir.exists()) dir.mkdirs()
        for (i in 0 until content.count) {
            val part: BodyPart = content.getBodyPart(i)
            val rawName = part.fileName
            val name = if (rawName.isNullOrBlank()) "" else MimeUtility.decodeText(rawName)
            if ((Part.ATTACHMENT.equals(part.disposition, true) || name.isNotBlank()) && matchesPhoneUpdateAttachment(name, expectedName)) {
                val saveName = if (name.endsWith(".apk.bin")) name else expectedName
                val file = File(dir, saveName)
                part.inputStream.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
                return file
            }
            val nested = part.content
            if (nested is Multipart) {
                val found = saveAttachment(nested, expectedName, dir)
                if (found != null) return found
            }
        }
        return null
    }

    private fun matchesPhoneUpdateAttachment(name: String, expectedName: String): Boolean {
        val normalName = name.replace(" ", "")
        val normalExpected = expectedName.replace(" ", "")
        if (normalName == normalExpected) return true
        return normalName.contains("PostBird-Phone") && normalName.endsWith(".apk.bin")
    }
}
