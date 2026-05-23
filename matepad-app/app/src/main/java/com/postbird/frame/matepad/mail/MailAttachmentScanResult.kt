package com.postbird.frame.matepad.mail

data class MailAttachmentScanResult(
    val success: Boolean,
    val message: String,
    val scannedMailCount: Int = 0,
    val matchedMailCount: Int = 0,
    val attachmentCount: Int = 0,
    val imageCount: Int = 0,
    val videoCount: Int = 0
) {
    fun toDisplayText(): String {
        return if (!success) {
            message
        } else {
            "已扫描 ${scannedMailCount} 封邮件\n" +
                "来自绑定邮箱的邮件 ${matchedMailCount} 封\n" +
                "发现可接收附件 ${attachmentCount} 个\n" +
                "图片 ${imageCount} 个，视频 ${videoCount} 个"
        }
    }
}
