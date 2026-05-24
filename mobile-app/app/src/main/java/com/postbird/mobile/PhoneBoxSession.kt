package com.postbird.mobile

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
        val store = Session.getInstance(props).getStore(PhoneBoxConfig.PROTOCOL)
        store.connect(PhoneBoxConfig.HOST, PhoneBoxConfig.PORT, address, pass)
        val folder = store.getFolder(PhoneBoxConfig.FOLDER)
        folder.open(Folder.READ_ONLY)
        return store to folder
    }
}
