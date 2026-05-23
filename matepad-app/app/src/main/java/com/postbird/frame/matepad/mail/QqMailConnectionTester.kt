package com.postbird.frame.matepad.mail

import java.net.SocketTimeoutException
import java.util.Properties
import javax.mail.AuthenticationFailedException
import javax.mail.Folder
import javax.mail.MessagingException
import javax.mail.Session

class QqMailConnectionTester {
    fun testConnection(account: String, credential: String): MailConnectionResult {
        val normalizedAccount = account.trim()

        if (normalizedAccount.isBlank() || credential.isBlank()) {
            return MailConnectionResult(false, "邮箱或凭证为空")
        }

        if (!normalizedAccount.endsWith("@qq.com", ignoreCase = true)) {
            return MailConnectionResult(false, "当前仅支持 QQ 邮箱")
        }

        val properties = Properties().apply {
            put("mail.store.protocol", "imaps")
            put("mail.imaps.host", QQ_IMAP_HOST)
            put("mail.imaps.port", QQ_IMAP_PORT.toString())
            put("mail.imaps.ssl.enable", "true")
            put("mail.imaps.connectiontimeout", CONNECTION_TIMEOUT_MS.toString())
            put("mail.imaps.timeout", READ_TIMEOUT_MS.toString())
            put("mail.imaps.writetimeout", WRITE_TIMEOUT_MS.toString())
        }

        var store: javax.mail.Store? = null
        var inbox: Folder? = null

        return try {
            val session = Session.getInstance(properties)
            store = session.getStore("imaps")
            store.connect(QQ_IMAP_HOST, QQ_IMAP_PORT, normalizedAccount, credential)
            inbox = store.getFolder("INBOX")
            inbox.open(Folder.READ_ONLY)
            MailConnectionResult(true, "邮箱连接成功，可以继续扫描可接收附件")
        } catch (error: AuthenticationFailedException) {
            MailConnectionResult(false, "邮箱凭证错误或 IMAP 未开启")
        } catch (error: SocketTimeoutException) {
            MailConnectionResult(false, "邮箱连接超时")
        } catch (error: MessagingException) {
            val rawMessage = error.message.orEmpty()
            val message = when {
                rawMessage.contains("authentication", ignoreCase = true) -> "邮箱凭证错误或 IMAP 未开启"
                rawMessage.contains("timeout", ignoreCase = true) -> "邮箱连接超时"
                rawMessage.contains("network", ignoreCase = true) -> "网络连接失败"
                else -> "邮箱连接失败：${rawMessage.ifBlank { "未知错误" }}"
            }
            MailConnectionResult(false, message)
        } catch (error: Exception) {
            MailConnectionResult(false, "未知错误：${error.javaClass.simpleName}")
        } finally {
            try { inbox?.close(false) } catch (_: Exception) {}
            try { store?.close() } catch (_: Exception) {}
        }
    }

    companion object {
        private const val QQ_IMAP_HOST = "imap.qq.com"
        private const val QQ_IMAP_PORT = 993
        private const val CONNECTION_TIMEOUT_MS = 15000
        private const val READ_TIMEOUT_MS = 15000
        private const val WRITE_TIMEOUT_MS = 15000
    }
}
