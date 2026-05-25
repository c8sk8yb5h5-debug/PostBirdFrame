package com.postbird.frame.matepad

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.postbird.frame.matepad.mail.MailMediaReceiver
import com.postbird.frame.matepad.mail.MediaReceiveResult
import com.postbird.frame.matepad.settings.MailSettingsStore
import com.postbird.frame.matepad.update.ApkInstallHelper
import com.postbird.frame.matepad.update.UpdateCoordinator
import com.postbird.frame.matepad.update.UpdatePackageResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

private const val INLINE_TIMEOUT_MS = 120000L

@Composable
fun InlineManualReceiveCard(
    onClose: () -> Unit,
    onStatus: (String) -> Unit,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val receiver = remember { MailMediaReceiver() }
    val settingsStore = remember(context) { MailSettingsStore(context) }
    var busy by remember { mutableStateOf(false) }
    var step by remember { mutableStateOf("1. 等待连接 QQ 邮箱") }
    var message by remember { mutableStateOf("未开始接收") }

    fun setMessage(text: String) {
        message = text
        onStatus(text)
        Toast.makeText(context.applicationContext, text, Toast.LENGTH_LONG).show()
    }

    InlineModalCard(width = 560.dp) {
        InlineHeader("手动收件", "在当前卡片内完成扫描、下载和刷新", onClose)
        InlineInfoRow("读取范围", "最近 20 封非更新邮件")
        InlineInfoRow("支持格式", "jpg / png / webp / gif / mp4 / mov / m4v")
        InlineInfoRow("保存规则", "自动跳过 APK 更新包，保存到相框素材目录")
        InlineStatusBox(step, message)
        Button(
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            onClick = {
                val settings = settingsStore.load()
                if (!settings.hasMailConfig) {
                    setMessage("请先保存 QQ 邮箱和授权码")
                    return@Button
                }
                busy = true
                step = "1. 正在连接 QQ 邮箱"
                setMessage("正在扫描最近邮件...")
                scope.launch {
                    val result = try {
                        withContext(Dispatchers.IO) {
                            withTimeout(INLINE_TIMEOUT_MS) {
                                receiver.receiveLatestMedia(context, settings.email, settings.authCode)
                            }
                        }
                    } catch (_: TimeoutCancellationException) {
                        MediaReceiveResult(false, "接收超时，请确认网络和附件大小")
                    } catch (error: Exception) {
                        MediaReceiveResult(false, "接收失败：${error.javaClass.simpleName}")
                    }
                    busy = false
                    step = if (result.success) "4. 接收完成，已刷新相框素材" else "4. 本轮未接收到新素材"
                    setMessage(result.message)
                    if (result.savedCount > 0) onRefresh()
                }
            }
        ) {
            Icon(Icons.Rounded.Download, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (busy) "正在接收..." else "立即接收")
        }
    }
}

@Composable
fun InlineVersionUpdateCard(
    onClose: () -> Unit,
    onStatus: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val coordinator = remember { UpdateCoordinator() }
    val installer = remember { ApkInstallHelper() }
    var busy by remember { mutableStateOf(false) }
    var step by remember { mutableStateOf("1. 准备读取当前版本") }
    var message by remember { mutableStateOf("未开始检查") }

    fun setMessage(text: String) {
        message = text
        onStatus(text)
        Toast.makeText(context.applicationContext, text, Toast.LENGTH_LONG).show()
    }

    InlineModalCard(width = 560.dp) {
        InlineHeader("版本更新", "在当前卡片内检查、下载并打开安装", onClose)
        InlineInfoRow("当前版本", "v${inlineVersionName(context)} / code=${inlineVersionCode(context)}")
        InlineInfoRow("更新邮件", "PostBirdFrame MatePad Update")
        InlineInfoRow("附件规则", "PostBird-MatePad-v版本-code编号.apk.bin")
        InlineInfoRow("处理流程", "扫描邮件 → 下载 .apk.bin → 还原 APK → 打开安装界面")
        InlineStatusBox(step, message)
        Button(
            enabled = !busy,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            onClick = {
                busy = true
                step = "1. 正在读取当前版本与邮箱设置"
                setMessage("正在检查 QQ 邮箱更新包...")
                scope.launch {
                    val result = try {
                        withContext(Dispatchers.IO) {
                            withTimeout(INLINE_TIMEOUT_MS) { coordinator.findUpdatePackage(context) }
                        }
                    } catch (_: TimeoutCancellationException) {
                        UpdatePackageResult(false, "QQ 邮箱更新检查超时", null)
                    } catch (error: Exception) {
                        UpdatePackageResult(false, "QQ 邮箱更新检查失败：${error.javaClass.simpleName}", null)
                    }
                    busy = false
                    if (!result.success || result.apkFile == null) {
                        step = "2. 未找到可安装更新包"
                        setMessage(result.message)
                        return@launch
                    }
                    step = "3. 更新包已准备好，准备打开安装界面"
                    setMessage(result.message)
                    if (!installer.canRequestPackageInstalls(context)) {
                        step = "4. 需要先允许安装未知应用"
                        setMessage("请允许本 APP 安装未知应用，然后返回重新检查更新")
                        installer.openInstallPermissionSettings(context)
                    } else {
                        val installResult = installer.installApk(context, result.apkFile)
                        step = if (installResult.success) "4. 已打开系统安装界面" else "4. 打开安装界面失败"
                        setMessage(installResult.message)
                    }
                }
            }
        ) {
            Icon(Icons.Rounded.SystemUpdate, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(if (busy) "正在处理..." else "检查并安装")
        }
    }
}

@Composable
private fun InlineModalCard(width: Dp, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.30f)), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.width(width).clip(RoundedCornerShape(28.dp)).background(Color(0xFFEFF8F4).copy(alpha = 0.98f)).padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) { content() }
    }
}

@Composable
private fun InlineHeader(title: String, subtitle: String, onClose: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, color = Color(0xFF102427), fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = Color(0xFF60726E), fontSize = 13.sp)
        }
        Text("×", color = Color(0xFF102427), fontSize = 30.sp, modifier = Modifier.clickable(onClick = onClose).padding(8.dp))
    }
}

@Composable
private fun InlineInfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color(0xFF60726E), fontSize = 13.sp, modifier = Modifier.width(76.dp))
        Text(value, color = Color(0xFF102427), fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun InlineStatusBox(step: String, message: String) {
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White).padding(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(step, color = Color(0xFF102427), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(message, color = Color(0xFF00796B), fontSize = 14.sp)
        }
    }
}

private fun inlineVersionName(context: Context): String {
    return try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "未知" } catch (_: Exception) { "未知" }
}

private fun inlineVersionCode(context: Context): Int {
    return try {
        val info = context.packageManager.getPackageInfo(context.packageName, 0)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) info.longVersionCode.toInt() else @Suppress("DEPRECATION") info.versionCode
    } catch (_: Exception) { 0 }
}
