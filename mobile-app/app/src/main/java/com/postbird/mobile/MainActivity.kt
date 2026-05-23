package com.postbird.mobile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.FileProvider
import org.json.JSONObject
import java.io.File
import java.net.URL
import java.security.MessageDigest
import kotlin.concurrent.thread

class MainActivity : Activity() {
    private lateinit var statusText: TextView

    private val latestJsonUrl = "https://raw.githubusercontent.com/c8sk8yb5h5-debug/PostBirdFrame/main/releases/mobile/latest.json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "PostBird Mobile"
            textSize = 26f
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "选择照片或视频，并通过绑定 QQ 邮箱发送到 MatePad 端。"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 24)
        }

        val updateButton = Button(this).apply {
            text = "检查更新"
            setOnClickListener { checkForUpdate() }
        }

        statusText = TextView(this).apply {
            text = "当前版本：0.2.0"
            textSize = 14f
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 0)
        }

        root.addView(title)
        root.addView(subtitle)
        root.addView(updateButton)
        root.addView(statusText)
        setContentView(root)
    }

    private fun checkForUpdate() {
        setStatus("正在检查更新...")
        thread {
            try {
                val jsonText = URL(latestJsonUrl).readText()
                val json = JSONObject(jsonText)
                val latestCode = json.getInt("versionCode")
                val latestName = json.getString("versionName")
                val apkUrl = json.getString("apkUrl")
                val sha256 = json.optString("sha256", "")

                if (latestCode <= 2) {
                    setStatus("当前已是最新版本：0.2.0")
                    return@thread
                }

                setStatus("发现新版本 $latestName，正在下载...")
                val apkFile = downloadApk(apkUrl)

                if (sha256.isNotBlank()) {
                    val actualSha = sha256(apkFile)
                    if (!actualSha.equals(sha256, ignoreCase = true)) {
                        apkFile.delete()
                        setStatus("更新包校验失败，已停止安装。")
                        return@thread
                    }
                }

                setStatus("下载完成，准备安装...")
                installApk(apkFile)
            } catch (e: Exception) {
                setStatus("检查更新失败：${e.message ?: "未知错误"}")
            }
        }
    }

    private fun downloadApk(apkUrl: String): File {
        val dir = File(cacheDir, "updates")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "postbird-mobile-update.apk")
        URL(apkUrl).openStream().use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file
    }

    private fun installApk(file: File) {
        runOnUiThread {
            if (android.os.Build.VERSION.SDK_INT >= 26 && !packageManager.canRequestPackageInstalls()) {
                setStatus("请先允许本 APP 安装未知应用，然后再次点击检查更新。")
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:$packageName")
                }
                startActivity(intent)
                return@runOnUiThread
            }

            val uri = FileProvider.getUriForFile(
                this,
                "$packageName.fileprovider",
                file
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        }
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun setStatus(message: String) {
        runOnUiThread {
            statusText.text = message
        }
    }
}
