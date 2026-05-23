package com.postbird.frame.matepad.update

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class GitHubReleaseClient(
    private val owner: String = "c8sk8yb5h5-debug",
    private val repo: String = "PostBirdFrame"
) {
    fun checkLatest(context: Context): UpdateInfo {
        val currentVersion = getCurrentVersionName(context)
        val endpoint = "https://api.github.com/repos/$owner/$repo/releases/latest"
        val connection = URL(endpoint).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 15000
        connection.readTimeout = 15000
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        connection.setRequestProperty("User-Agent", "PostBirdFrame-MatePad")

        return try {
            val code = connection.responseCode
            if (code !in 200..299) {
                return UpdateInfo(false, currentVersion, "", "", "", "检查更新失败：HTTP $code")
            }

            val text = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(text)
            val tagName = json.optString("tag_name")
            val assets = json.optJSONArray("assets")
            var apkName = ""
            var apkUrl = ""

            if (assets != null) {
                for (index in 0 until assets.length()) {
                    val asset = assets.optJSONObject(index) ?: continue
                    val name = asset.optString("name")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkName = name
                        apkUrl = asset.optString("browser_download_url")
                        break
                    }
                }
            }

            if (apkUrl.isBlank()) {
                UpdateInfo(false, currentVersion, tagName, "", "", "最新 Release 中没有找到 APK")
            } else {
                val available = tagName.isNotBlank() && !tagName.contains(currentVersion, ignoreCase = true)
                UpdateInfo(
                    available = available,
                    currentVersionName = currentVersion,
                    latestTagName = tagName,
                    apkName = apkName,
                    apkDownloadUrl = apkUrl,
                    message = if (available) "发现新版本：$tagName" else "当前已是最新版本"
                )
            }
        } catch (error: Exception) {
            UpdateInfo(false, currentVersion, "", "", "", "检查更新失败：${error.javaClass.simpleName}")
        } finally {
            connection.disconnect()
        }
    }

    private fun getCurrentVersionName(context: Context): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "0.0.0"
        } catch (_: Exception) {
            "0.0.0"
        }
    }
}
