package com.postbird.frame.matepad.update

import android.content.Context
import java.io.File

class ApkDownloader {
    fun download(context: Context, url: String, fileName: String = "PostBird-MatePad-update.apk"): File {
        throw IllegalStateException("APP 内更新只使用 QQ 邮箱更新包")
    }
}
