package com.postbird.mobile

import javax.mail.Multipart

object MailContentReader {
    fun textOf(content: Any?): String {
        return when (content) {
            is String -> content
            is Multipart -> {
                val builder = StringBuilder()
                for (i in 0 until content.count) {
                    builder.append(textOf(content.getBodyPart(i).content)).append('\n')
                }
                builder.toString()
            }
            else -> ""
        }
    }
}
