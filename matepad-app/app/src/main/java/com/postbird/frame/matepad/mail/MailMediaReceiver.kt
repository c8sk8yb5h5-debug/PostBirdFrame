package com.postbird.frame.matepad.mail

import android.content.Context
import java.net.SocketTimeoutException
import java.util.Properties
import javax.mail.AuthenticationFailedException
import javax.mail.FetchProfile
import javax.mail.Folder
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.Multipart
import javax.mail.Part
import javax.mail.Session

class MailMediaReceiver {
    fun receiveLatestMedia(
        context: Context,
        email: String,
        authCode: String,
        maxMessages: Int = 20
    ): MediaReceiveResult {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isBlank() || authCode.isBlank()) return MediaReceiveResult(false, "邮箱或授权码为空")
        if (!normalizedEmail.endsWith("@qq.com", ignoreCase = true)) return MediaReceiveResult(false, "当前仅支持 QQ 邮箱")

        val properties = Properties().apply {
            put("mail.store.protocol", "imaps")
            put("mail.imaps.host", QQ_IMAP_HOST)
            put("mail.imaps.port", QQ_IMAP_PORT.toString())
            put("mail.imaps.ssl.enable", "true")
            put("mail.imaps.connectiontimeout", TIMEOUT_MS.toString())
            put("mail.imaps.timeout", LONG_TIMEOUT_MS.toString())
            put("mail.imaps.writetimeout", LONG_TIMEOUT_MS.toString())
            put("mail.imaps.partialfetch", "false")
            put("mail.imaps.fetchsize", FETCH_SIZE_BYTES.toString())
        }

        var store: javax.mail.Store? = null
        var inbox: Folder? = null
        val mediaStore = MediaReceiveStore(context)

        return try {
            val session = Session.getInstance(properties)
            store = session.getStore("imaps")
            store.connect(QQ_IMAP_HOST, QQ_IMAP_PORT, normalizedEmail, authCode)
            inbox = store.getFolder("INBOX")
            inbox.open(Folder.READ_ONLY)

            val total = inbox.messageCount
            if (total <= 0) return MediaReceiveResult(false, "收件箱暂无邮件")

            val start = (total - maxMessages + 1).coerceAtLeast(1)
            val messages = inbox.getMessages(start, total)
            inbox.fetch(messages, FetchProfile().apply { add(FetchProfile.Item.ENVELOPE) })

            var saved = 0
            var skipped = 0
            for (message in messages.reversed()) {
                if (!isFromBoundEmail(message, normalizedEmail)) continue
                if (isUpdateMail(message.subject.orEmpty())) continue
                val result = saveFromPart(message, mediaStore)
                saved += result.saved
                skipped += result.skipped
            }

            if (saved > 0) {
                MediaReceiveResult(true, "已接收 $saved 个照片/视频附件", saved, skipped)
            } else {
                MediaReceiveResult(false, "最近 $maxMessages 封邮件中未发现新的照片/视频附件", saved, skipped)
            }
        } catch (error: AuthenticationFailedException) {
            MediaReceiveResult(false, "邮箱授权码错误或 IMAP 未开启")
        } catch (error: SocketTimeoutException) {
            MediaReceiveResult(false, "邮箱连接或附件下载超时")
        } catch (error: MessagingException) {
            MediaReceiveResult(false, "邮箱附件接收失败：${error.message.orEmpty().ifBlank { "未知错误" }}")
        } catch (error: Exception) {
            MediaReceiveResult(false, "附件接收失败：${error.javaClass.simpleName}")
        } finally {
            try { inbox?.close(false) } catch (_: Exception) {}
            try { store?.close() } catch (_: Exception) {}
        }
    }

    private fun isFromBoundEmail(message: Message, boundEmail: String): Boolean {
        val fromText = message.from?.joinToString(separator = ",") { it.toString() }.orEmpty()
        return fromText.contains(boundEmail, ignoreCase = true)
    }

    private fun isUpdateMail(subject: String): Boolean {
        return subject.contains("PostBirdFrame", ignoreCase = true) && subject.contains("Update", ignoreCase = true)
    }

    private fun saveFromPart(part: Part, mediaStore: MediaReceiveStore): SaveCount {
        if (part.isMimeType("multipart/*")) {
            val content = part.content
            if (content is Multipart) {
                var saved = 0
                var skipped = 0
                for (index in 0 until content.count) {
                    val result = saveFromPart(content.getBodyPart(index), mediaStore)
                    saved += result.saved
                    skipped += result.skipped
                }
                return SaveCount(saved, skipped)
            }
            return SaveCount()
        }

        val fileName = part.fileName.orEmpty().substringAfterLast('/').substringAfterLast('\\')
        if (!mediaStore.isSupportedMedia(fileName)) return SaveCount(skipped = if (fileName.isNotBlank()) 1 else 0)

        val target = mediaStore.createTargetFile(fileName)
        part.inputStream.use { input ->
            target.outputStream().use { output ->
                val buffer = ByteArray(COPY_BUFFER_BYTES)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    output.write(buffer, 0, count)
                }
            }
        }
        return SaveCount(saved = 1)
    }

    private data class SaveCount(val saved: Int = 0, val skipped: Int = 0)

    companion object {
        private const val QQ_IMAP_HOST = "imap.qq.com"
        private const val QQ_IMAP_PORT = 993
        private const val TIMEOUT_MS = 30000
        private const val LONG_TIMEOUT_MS = 180000
        private const val FETCH_SIZE_BYTES = 1048576
        private const val COPY_BUFFER_BYTES = 65536
    }
}
