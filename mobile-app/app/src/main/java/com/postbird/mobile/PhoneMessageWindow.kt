package com.postbird.mobile

import javax.mail.Folder
import javax.mail.Message

object PhoneMessageWindow {
    fun latest(folder: Folder, limit: Int): Array<Message> {
        val total = folder.messageCount
        if (total <= 0) return emptyArray()
        val safeLimit = limit.coerceAtLeast(1)
        val start = kotlin.math.max(1, total - safeLimit + 1)
        return folder.getMessages(start, total).asReversedArray()
    }
}
