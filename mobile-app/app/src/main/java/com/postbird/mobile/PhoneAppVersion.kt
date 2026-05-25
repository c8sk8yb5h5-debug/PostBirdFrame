package com.postbird.mobile

import android.content.Context
import android.os.Build

object PhoneAppVersion {
    fun code(context: Context): Int {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        return if (Build.VERSION.SDK_INT >= 28) info.longVersionCode.toInt() else @Suppress("DEPRECATION") info.versionCode
    }

    fun name(context: Context): String {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        return info.versionName ?: ""
    }
}
