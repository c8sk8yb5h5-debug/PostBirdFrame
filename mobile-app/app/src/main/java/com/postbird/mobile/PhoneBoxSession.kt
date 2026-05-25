package com.postbird.mobile

import java.net.InetAddress
import java.util.Properties
import javax.mail.Folder
import javax.mail.Session
import javax.mail.Store

object PhoneBoxSession {
    fun open(address: String, pass: String): Pair<Store, Folder> {
        val props = Properties()
        props["mail.store.protocol"] = PhoneBoxConfig.PROTOCOL
        props["mail.${PhoneBoxConfig.PROTOCOL}.host"] = PhoneBoxConfig.HOST
        props["mail.${PhoneBoxConfig.PROTOCOL}.port"] = PhoneBoxConfig.PORT.toString()
        props["mail.${PhoneBoxConfig.PROTOCOL}.ssl.enable"] = "true"
        props["mail.${PhoneBoxConfig.PROTOCOL}.connectiontimeout"] = "30000"
        props["mail.${PhoneBoxConfig.PROTOCOL}.timeout"] = "180000"
        props["mail.${PhoneBoxConfig.PROTOCOL}.writetimeout"] = "180000"
        props["mail.${PhoneBoxConfig.PROTOCOL}.partialfetch"] = "false"
        props["mail.${PhoneBoxConfig.PROTOCOL}.fetchsize"] = "1048576"
        props["mail.${PhoneBoxConfig.PROTOCOL}.connectionpooltimeout"] = "180000"
        props["mail.${PhoneBoxConfig.PROTOCOL}.ssl.protocols"] = "TLSv1.2"
        props["mail.${PhoneBoxConfig.PROTOCOL}.ssl.trust"] = PhoneBoxConfig.HOST

        try {
            InetAddress.getByName(PhoneBoxConfig.HOST)
        } catch (e: Exception) {
            throw IllegalStateException("无法解析 QQ 邮箱服务器，请检查手机网络或 DNS")
        }

        val store = Session.getInstance(props).getStore(PhoneBoxConfig.PROTOCOL)
        try {
            store.connect(PhoneBoxConfig.HOST, PhoneBoxConfig.PORT, address, pass)
        } catch (e: Exception) {
            throw IllegalStateException("无法连接 QQ 邮箱 IMAP。请确认手机可联网、QQ邮箱已开启IMAP/SMTP服务、授权码正确。原始错误：${e.message ?: "未知错误"}")
        }

        val folder = store.getFolder(PhoneBoxConfig.FOLDER)
        folder.open(Folder.READ_ONLY)
        return store to folder
    }
}
