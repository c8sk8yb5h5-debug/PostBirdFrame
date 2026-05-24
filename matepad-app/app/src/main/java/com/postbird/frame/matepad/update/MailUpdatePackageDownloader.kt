package com.postbird.frame.matepad.update

import android.content.Context
import android.os.Environment
import java.io.File
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

class MailUpdatePackageDownloader {
    fun downloadLatestUpdate(
        context: Context,
        email: String,
        authCode: String,
        currentVersionCode: Int,
        maxMessages: Int = 20
    ): MailUpdateInfo {
        val normalizedEmail = email.trim()
        if (normalizedEmail.isBlank() || authCode.isBlank()) {
            return MailUpdateInfo(false, "邮箱或授权码为空")
        }
        if (!normalizedEmail.endsWith("@qq.com", ignoreCase = true)) {
            return MailUpdateInfo(false, "当前仅支持 QQ 邮箱")
        }

        val properties = Properties().apply {
            put("mail.store.protocol", "imaps")
            put("mail.imaps.host", QQ_IMAP_HOST)
            put("mail.imaps.port", QQ_IMAP_PORT.toString())
            put("mail.imaps.ssl.enable", "true")
            put("mail.imaps.connectiontimeout", CONNECT_TIMEOUT_MS.toString())
            put("mail.imaps.timeout", READ_TIMEOUT_MS.toString())
            put("mail.imaps.writetimeout", READ_TIMEOUT_MS.toString())
            put("mail.imaps.partialfetch", "false")
            put("mail.imaps.fetchsize", FETCH_SIZE_BYTES.toString())
            put("mail.imaps.connectionpooltimeout", READ_TIMEOUT_MS.toString())
        }

        var store: javax.mail.Store? = null
        var inbox: Folder? = null

        return try {
            val session = Session.getInstance(properties)
            store = session.getStore("imaps")
            store.connect(QQ_IMAP_HOST, QQ_IMAP_PORT, normalizedEmail, authCode)
            inbox = store.getFolder("INBOX")
            inbox.open(Folder.READ_ONLY)

            val total = inbox.messageCount
            if (total <= 0) return MailUpdateInfo(false, "收件箱暂无邮件")
            val start = (total - maxMessages + 1).coerceAtLeast(1)
            val messages = inbox.getMessages(start, total)
            inbox.fetch(messages, FetchProfile().apply {
                add(FetchProfile.Item.ENVELOPE)
                add(FetchProfile.Item.FLAGS)
            })

            for (message in messages.reversed()) {
                val subject = message.subject.orEmpty()
                if (!subject.contains(UPDATE_SUBJECT_KEY, ignoreCase = true)) continue
                if (!isFromBoundEmail(message, normalizedEmail)) continue

                val versionCode = parseVersionCode(subject)
                val versionName = parseVersionName(subject)
                if (versionCode <= currentVersionCode) continue

                val file = saveFirstUpdateAttachment(context, message) ?: continue
                val apkFile = MailUpdatePackageInstaller().prepareApk(context, file)
                return MailUpdateInfo(
                    success = true,
                    message = "发现 MatePad 邮箱更新包：v$versionName code=$versionCode",
                    versionName = versionName,
                    versionCode = versionCode,
                    attachmentName = file.name,
                    apkFile = apkFile
                )
            }

            MailUpdateInfo(false, "未发现 MatePad 端可用更新包")
        } catch (error: AuthenticationFailedException) {
            MailUpdateInfo(false, "邮箱授权码错误或 IMAP 未开启")
        } catch (error: SocketTimeoutException) {
            MailUpdateInfo(false, "邮箱连接或附件下载超时")
        } catch (error: MessagingException) {
            MailUpdateInfo(false, "邮箱更新检查失败：${error.message.orEmpty().ifBlank { "未知错误" }}")
        } catch (error: Exception) {
            val type = error.javaClass.simpleName
            val hint = if (type.contains("FolderClosed", ignoreCase = true)) {
                "，可能是 QQ 邮箱在读取大附件时断开连接，请重试或发送更新邮件到收件箱顶部"
            } else {
                ""
            }
            MailUpdateInfo(false, "邮箱更新失败：$type$hint")
        } finally {
            try { inbox?.close(false) } catch (_: Exception) {}
            try { store?.close() } catch (_: Exception) {}
        }
    }

    private fun isFromBoundEmail(message: Message, boundEmail: String): Boolean {
        val fromText = message.from?.joinToString(separator = ",") { it.toString() }.orEmpty()
        return fromText.contains(boundEmail, ignoreCase = true)
    }

    private fun parseVersionCode(subject: String): Int {
        val regex = Regex("code\\s*=\\s*(\\d+)", RegexOption.IGNORE_CASE)
        return regex.find(subject)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: 0
    }

    private fun parseVersionName(subject: String): String {
        val regex = Regex("v([0-9]+(?:\\.[0-9]+){1,3})", RegexOption.IGNORE_CASE)
        return regex.find(subject)?.groupValues?.getOrNull(1).orEmpty()
    }

    private fun saveFirstUpdateAttachment(context: Context, part: Part): File? {
        if (part.isMimeType("multipart/*")) {
            val content = part.content
            if (content is Multipart) {
                for (index in 0 until content.count) {
                    val found = saveFirstUpdateAttachment(context, content.getBodyPart(index))
                    if (found != null) return found
                }
            }
            return null
        }

        val fileName = part.fileName.orEmpty().substringAfterLast('/')
        if (!isUpdateAttachment(fileName)) return null

        val updatesDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "mail_updates")
        if (!updatesDir.exists()) updatesDir.mkdirs()
        val outputFile = File(updatesDir, fileName)
        if (outputFile.exists()) outputFile.delete()

        part.inputStream.use { input ->
            outputFile.outputStream().use { output ->
                val buffer = ByteArray(COPY_BUFFER_BYTES)
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

    private fun isUpdateAttachment(fileName: String): Boolean {
        val supportedSuffix = fileName.endsWith(".apk", ignoreCase = true) ||
            fileName.endsWith(".apk.zip", ignoreCase = true) ||
            fileName.endsWith(".apk.bin", ignoreCase = true) ||
            fileName.endsWith(".zip", ignoreCase = true)
        return fileName.contains(ATTACHMENT_NAME_KEY, ignoreCase = true) && supportedSuffix
    }

    companion object {
        private const val QQ_IMAP_HOST = "imap.qq.com"
        private const val QQ_IMAP_PORT = 993
        private const val CONNECT_TIMEOUT_MS = 30000
        private const val READ_TIMEOUT_MS = 180000
        private const val FETCH_SIZE_BYTES = 1048576
        private const val COPY_BUFFER_BYTES = 65536
        private const val UPDATE_SUBJECT_KEY = "PostBirdFrame MatePad Update"
        private const val ATTACHMENT_NAME_KEY = "PostBird-MatePad"
    }
}
