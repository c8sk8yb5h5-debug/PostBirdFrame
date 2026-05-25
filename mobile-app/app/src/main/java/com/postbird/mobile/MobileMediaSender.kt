package com.postbird.mobile

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.sun.mail.util.ByteArrayDataSource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Properties
import javax.activation.DataHandler
import javax.mail.Message
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart

object MobileMediaSender {
    fun send(context: Context, fromAddress: String, appCode: String, toAddress: String, items: List<Uri>): PhoneSendResult {
        val from = fromAddress.trim()
        val to = toAddress.trim().ifBlank { from }
        if (from.isBlank() || appCode.isBlank()) return PhoneSendResult(false, "请先填写邮箱和授权码")
        if (items.isEmpty()) return PhoneSendResult(false, "请先选择照片或视频")

        return try {
            val props = Properties().apply {
                put("mail.smtp.host", "smtp.qq.com")
                put("mail.smtp.port", "465")
                put("mail.smtp.auth", "true")
                put("mail.smtp.ssl.enable", "true")
                put("mail.smtp.connectiontimeout", "30000")
                put("mail.smtp.timeout", "180000")
                put("mail.smtp.writetimeout", "180000")
                put("mail.smtp.ssl.protocols", "TLSv1.2")
            }
            val session = Session.getInstance(props)
            val msg = MimeMessage(session)
            msg.setFrom(InternetAddress(from))
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false))
            msg.subject = "PostBirdFrame Media Delivery ${stamp()}"

            val mixed = MimeMultipart()
            MimeBodyPart().also { part ->
                part.setText("platform=phone\npurpose=media_delivery\nattachmentCount=${items.size}\nreceiver=matepad", "UTF-8")
                mixed.addBodyPart(part)
            }
            items.forEachIndexed { index, uri ->
                val fileName = displayName(context, uri).ifBlank { "PostBird-Media-${index + 1}" }
                val type = context.contentResolver.getType(uri) ?: fallbackMime(fileName)
                val data = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: return PhoneSendResult(false, "无法读取文件：$fileName")
                MimeBodyPart().also { part ->
                    part.dataHandler = DataHandler(ByteArrayDataSource(data, type))
                    part.setFileName(fileName)
                    mixed.addBodyPart(part)
                }
            }
            msg.setContent(mixed)
            Transport.send(msg, from, appCode.trim().replace(" ", ""))
            PhoneSendResult(true, "已发送 ${items.size} 个照片/视频附件")
        } catch (error: Exception) {
            PhoneSendResult(false, "发送失败：${error.javaClass.simpleName} ${error.message.orEmpty()}".trim())
        }
    }

    private fun stamp(): String = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())

    private fun displayName(context: Context, uri: Uri): String {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0 && cursor.moveToFirst()) cursor.getString(index).orEmpty() else ""
            }.orEmpty()
        } catch (_: Exception) { "" }
    }

    private fun fallbackMime(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
            lower.endsWith(".png") -> "image/png"
            lower.endsWith(".webp") -> "image/webp"
            lower.endsWith(".gif") -> "image/gif"
            lower.endsWith(".mp4") -> "video/mp4"
            lower.endsWith(".mov") -> "video/quicktime"
            lower.endsWith(".m4v") -> "video/x-m4v"
            else -> "application/octet-stream"
        }
    }
}

data class PhoneSendResult(val success: Boolean, val message: String)
