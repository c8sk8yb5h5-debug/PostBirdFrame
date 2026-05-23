package com.postbird.frame.matepad.update

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UpdateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { UpdateScreen() } }
    }
}

@Composable
private fun UpdateScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val client = remember { GitHubReleaseClient() }
    val downloader = remember { ApkDownloader() }
    val installer = remember { ApkInstallHelper() }
    var status by remember { mutableStateOf("点击检查更新，APP 会从 GitHub Releases 获取最新 APK。") }
    var busy by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("邮差鸟相框更新", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text(status)
        Spacer(Modifier.height(24.dp))
        Button(
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                busy = true
                status = "正在检查更新..."
                scope.launch {
                    val info = withContext(Dispatchers.IO) { client.checkLatest(context) }
                    if (info.apkDownloadUrl.isBlank()) {
                        busy = false
                        status = info.message
                        Toast.makeText(context, info.message, Toast.LENGTH_LONG).show()
                        return@launch
                    }

                    status = "发现更新，正在下载 APK..."
                    val apkFile = try {
                        withContext(Dispatchers.IO) { downloader.download(context, info.apkDownloadUrl) }
                    } catch (error: Exception) {
                        busy = false
                        status = "下载失败：${error.javaClass.simpleName}"
                        Toast.makeText(context, status, Toast.LENGTH_LONG).show()
                        return@launch
                    }

                    busy = false
                    if (!installer.canRequestPackageInstalls(context)) {
                        status = "请允许本 APP 安装未知应用，然后返回重新点击检查更新。"
                        Toast.makeText(context, status, Toast.LENGTH_LONG).show()
                        installer.openInstallPermissionSettings(context)
                    } else {
                        status = "下载完成，正在打开安装界面。"
                        Toast.makeText(context, status, Toast.LENGTH_LONG).show()
                        installer.installApk(context, apkFile)
                    }
                }
            }
        ) { Text(if (busy) "正在处理..." else "检查更新并安装") }
    }
}
