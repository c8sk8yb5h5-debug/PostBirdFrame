package com.postbird.frame.matepad.mail

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
import com.postbird.frame.matepad.settings.MailSettingsStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class ReceiveMediaActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { ReceiveMediaScreen() } }
    }
}

@Composable
private fun ReceiveMediaScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val receiver = remember { MailMediaReceiver() }
    val store = remember(context) { MailSettingsStore(context) }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("点击下方按钮，从 QQ 邮箱读取照片/视频附件。") }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("接收照片 / 视频", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))
        Text(status)
        Spacer(Modifier.height(24.dp))
        Button(
            enabled = !busy,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val settings = store.load()
                if (!settings.hasMailConfig) {
                    status = "请先在设置页保存 QQ 邮箱和授权码"
                    Toast.makeText(context, status, Toast.LENGTH_LONG).show()
                    return@Button
                }

                busy = true
                status = "正在检查最近邮件并下载照片/视频附件，最多等待 2 分钟..."
                scope.launch {
                    val result = try {
                        withContext(Dispatchers.IO) {
                            withTimeout(RECEIVE_TIMEOUT_MS) {
                                receiver.receiveLatestMedia(
                                    context = context,
                                    email = settings.email,
                                    authCode = settings.authCode
                                )
                            }
                        }
                    } catch (_: TimeoutCancellationException) {
                        MediaReceiveResult(false, "接收超时，请确认网络和附件大小")
                    } catch (error: Exception) {
                        MediaReceiveResult(false, "接收失败：${error.javaClass.simpleName}")
                    }

                    busy = false
                    status = result.message
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        ) {
            Text(if (busy) "正在接收..." else "立即检查并接收")
        }
    }
}

private const val RECEIVE_TIMEOUT_MS = 120000L
