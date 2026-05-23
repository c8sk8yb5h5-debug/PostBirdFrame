package com.postbird.frame.matepad.mail

import java.net.SocketTimeoutException
import java.util.Properties
import javax.mail.AuthenticationFailedException
import javax.mail.BodyPart
import javax.mail.Folder
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Multipart
import javax.mail.Part
import javax.mail.Session

class QqMailAttachmentScanner {
    fun scanRecentAttachments(
        email: String,
        authCode: String,
        maxMessages: Int = DEFAULT_SCAN_COUNT
    ): MailAttachmentScanResult {
        val normalizedEmail = email.trim()

        if (normalizedEmail.isBlank() || authCode.isBlank()) {
            return MailAttachmentScanResult(false, "邮箱或授权码为空")
        }

        if (!normalizedEmail.endsWith("@qq.com", ignoreCase = true)) {
            return MailAttachmentScanResult(false, "当前仅支持 QQ 邮箱")
        }

        val properties = Properties().apply {
            put("mail.store.protocol", "imaps")
            put("mail.imaps.host", QQ_IMAP_HOST)
            put("mail.imaps.port", QQ_IMAP_PORT.toString())
            put("mail.imaps.ssl.enable", "true")
            put("mail.imaps.connectiontimeout", TIMEOUT_MS.toString())
            put("mail.imaps.timeout", TIMEOUT_MS.toString())
            put("mail.imaps.writetimeout", TIMEOUT_MS.toString())
        }

        var store: javax.mail.Store? = null
        var inbox: Folder? = null

        return try {
            val session = Session.getInstance(properties)
            store = session.getStore("imaps")
            store.connect(QQ_IMAP_HOST, QQ_IMAP_PORT, normalizedEmail, authCode)

            inbox = store.getFolder("INBOX")
            inbox.open(Folder.READ_ONLY)

            val totalMessages = inbox.messageCount
            if (totalMessages <= 0) {
                return MailAttachmentScanResult(
                    success = true,
                    message = "收件箱暂无邮件",
                    scannedMailCount = 0,
                    matchedMailCount = 0,
                    attachmentCount = 0,
                    imageCount = 0,
                    videoCount = 0
                )
            }

            val end = totalMessages
            val start = (totalMessages - maxMessages + 1).coerceAtLeast(1)
            val messages = inbox.getMessages(start, end).reversed()

            var scannedMailCount = 0
            var matchedMailCount = 0
            var imageCount = 0
            var videoCount = 0

            for (message in messages) {
                scannedMailCount++
                if (!isFromBoundEmail(message, normalizedEmail)) continue

                matchedMailCount++
                val count = countAcceptedAttachments(message)
                imageCount += count.first
                videoCount += count.second
            }

            val attachmentCount = imageCount + videoCount
            MailAttachmentScanResult(
                success = true,
                message = "扫描完成",
                scannedMailCount = scannedMailCount,
                matchedMailCount = matchedMailCount,
                attachmentCount = attachmentCount,
                imageCount = imageCount,
                videoCount = videoCount
            )
        } catch (error: AuthenticationFailedException) {
            MailAttachmentScanResult(false, "邮箱授权码错误或 IMAP 未开启")
        } catch (error: SocketTimeoutException) {
            MailAttachmentScanResult(false, "邮箱连接超时")
        } catch (error: MessagingException) {
            val rawMessage = error.message.orEmpty()
            val message = when {
                rawMessage.contains("authentication", ignoreCase = true) -> "邮箱授权码错误或 IMAP 未开启"
                rawMessage.contains("timeout", ignoreCase = true) -> "邮箱连接超时"
                rawMessage.contains("network", ignoreCase = true) -> "网络连接失败"
                else -> "扫描失败：${rawMessage.ifBlank { "未知错误" }}"
            }
            MailAttachmentScanResult(false, message)
        } catch (error: Exception) {
            MailAttachmentScanResult(false, "未知错误：${error.javaClass.simpleName}")
        } finally {
            try {
                inbox?.close(false)
            } catch (_: Exception) {
            }

            try {
                store?.close()
            } catch (_: Exception) {
            }
        }
    }

    private fun isFromBoundEmail(message: Message, boundEmail: String): Boolean {
        val fromText = message.from?.joinToString(separator = ",") { it.toString() }.orEmpty()
        return fromText.contains(boundEmail, ignoreCase = true)
    }

    private fun countAcceptedAttachments(part: Part): Pair<Int, Int> {
        var imageCount = 0
        var videoCount = 0

        if (Part.ATTACHMENT.equals(part.disposition, ignoreCase = true) || part.fileName != null) {
            val fileName = part.fileName.orEmpty()
            val extension = fileName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
            when (extension) {
                "jpg", "jpeg", "png", "webp" -> imageCount++
                "mp4", "mov", "m4v" -> videoCount++
            }
        }

        val content = runCatching { part.content }.getOrNull()
        if (content is Multipart) {
            for (index in 0 until content.count) {
                val bodyPart: BodyPart = content.getBodyPart(index)
                val childCount = countAcceptedAttachments(bodyPart)
                imageCount += childCount.first
                videoCount += childCount.second
            }
        }

        return imageCount to videoCount
    }

    companion object {
        private const val QQ_IMAP_HOST = "imap.qq.com"
        private const val QQ_IMAP_PORT = 993
        private const val TIMEOUT_MS = 15000
        private const val DEFAULT_SCAN_COUNT = 20
    }
}
