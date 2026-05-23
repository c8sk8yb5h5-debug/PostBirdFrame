package com.postbird.frame.matepad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.MarkEmailRead
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.postbird.frame.matepad.mail.QqMailConnectionTester
import com.postbird.frame.matepad.settings.MailSettings
import com.postbird.frame.matepad.settings.MailSettingsStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PostBirdFrameTheme {
                MatePadFrameScreen()
            }
        }
    }
}

@Composable
private fun PostBirdFrameTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF6E8F7C),
            secondary = Color(0xFFE8B85F),
            background = Color(0xFFF6EFE2),
            surface = Color(0xFFFFF8EC),
            onPrimary = Color.White,
            onSecondary = Color(0xFF3E3420),
            onBackground = Color(0xFF2E3A32),
            onSurface = Color(0xFF2E3A32)
        ),
        content = content
    )
}

@Composable
private fun MatePadFrameScreen() {
    var settingsVisible by remember { mutableStateOf(false) }
    val contentAlpha by animateFloatAsState(
        targetValue = if (settingsVisible) 0.72f else 1f,
        label = "contentAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFFE6F5F0),
                        Color(0xFFFFF4D8),
                        Color(0xFFEAF3DF)
                    )
                )
            )
    ) {
        FrameMainContent(
            modifier = Modifier
                .fillMaxSize()
                .alpha(contentAlpha),
            settingsVisible = settingsVisible
        )

        AnimatedVisibility(
            visible = settingsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(2f)
        ) {
            SettingsOverlay(
                onClose = { settingsVisible = false }
            )
        }

        FloatingActionButton(
            onClick = { settingsVisible = !settingsVisible },
            containerColor = Color(0xFF6E8F7C),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(28.dp)
                .size(64.dp)
                .zIndex(3f)
        ) {
            Icon(
                imageVector = if (settingsVisible) Icons.Rounded.Close else Icons.Rounded.Settings,
                contentDescription = if (settingsVisible) "关闭设置" else "打开设置"
            )
        }
    }
}

@Composable
private fun FrameMainContent(
    modifier: Modifier,
    settingsVisible: Boolean
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "邮差鸟相框",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF2E3A32)
            )
            Text(
                text = "MatePad 横屏播放端 · 第四轮邮箱连接测试",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF637568)
            )
        }

        BoxWithConstraints(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 96.dp, vertical = 72.dp)
        ) {
            val mediaModifier = if (maxWidth / maxHeight > 1.8f) {
                Modifier
                    .fillMaxHeight(0.72f)
                    .aspectRatio(16f / 10f)
            } else {
                Modifier
                    .fillMaxWidth(0.72f)
                    .aspectRatio(16f / 10f)
            }

            EmptyMediaCard(
                modifier = mediaModifier.align(Alignment.Center),
                settingsVisible = settingsVisible
            )
        }

        Text(
            text = "等待手机端发送照片或视频",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF64736A),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 34.dp)
        )
    }
}

@Composable
private fun EmptyMediaCard(
    modifier: Modifier,
    settingsVisible: Boolean
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(34.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (settingsVisible) 2.dp else 10.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBF2))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, Color(0xFFE4D8C4), RoundedCornerShape(34.dp))
                .padding(36.dp)
        ) {
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                PostBirdPlaceholder()

                Text(
                    text = "还没有收到照片",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color(0xFF2E3A32)
                )

                Text(
                    text = "手机端发送照片或视频后，会自动进入这里播放。\n第四轮用于验证 QQ 邮箱 IMAP 连接。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF66756A),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PostBirdPlaceholder() {
    Box(
        modifier = Modifier
            .size(116.dp)
            .background(Color(0xFFF7DFA2), CircleShape)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 18.dp)
                .size(58.dp)
                .background(Color(0xFF6E8F7C), CircleShape)
        )

        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 35.dp),
            horizontalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color.White, CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(5.dp)
                        .background(Color(0xFF2E3A32), CircleShape)
                )
            }

            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color.White, CircleShape)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(5.dp)
                        .background(Color(0xFF2E3A32), CircleShape)
                )
            }
        }

        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 20.dp)
                .width(72.dp)
                .height(44.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE8B85F), RoundedCornerShape(8.dp))
            ) {
                Text(
                    text = "✉",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color(0xFFE8B85F)
                )
            }
        }
    }
}

@Composable
private fun SettingsOverlay(onClose: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.18f))
            .padding(28.dp)
    ) {
        SettingsPanel(
            onClose = onClose,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .widthIn(min = 456.dp, max = 540.dp)
                .fillMaxHeight()
        )
    }
}

@Composable
private fun SettingsPanel(
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val store = remember(context) { MailSettingsStore(context) }
    val connectionTester = remember { QqMailConnectionTester() }
    val coroutineScope = rememberCoroutineScope()
    val initialSettings = remember { store.load() }

    var email by remember { mutableStateOf(initialSettings.email) }
    var authCode by remember { mutableStateOf(initialSettings.authCode) }
    var authCodeVisible by remember { mutableStateOf(false) }
    var autoCheckEnabled by remember { mutableStateOf(initialSettings.autoCheckEnabled) }
    var lastSavedAt by remember { mutableStateOf(initialSettings.lastSavedAt) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var statusMessage by remember {
        mutableStateOf(
            when {
                !store.isSecureStorageReady() -> store.getStorageStatusText()
                initialSettings.hasMailConfig -> "已配置，尚未连接"
                else -> "未配置"
            }
        )
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8EC))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(26.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "设置",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color(0xFF2E3A32)
                    )
                    Text(
                        text = "加密配置与 QQ 邮箱连接测试",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6F7A70)
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "关闭设置"
                    )
                }
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("QQ 邮箱") },
                placeholder = { Text("example@qq.com") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = authCode,
                onValueChange = { authCode = it },
                label = { Text("QQ 邮箱授权码") },
                placeholder = { Text("请输入 QQ 邮箱授权码") },
                singleLine = true,
                visualTransformation = if (authCodeVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { authCodeVisible = !authCodeVisible }) {
                        Icon(
                            imageVector = if (authCodeVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                            contentDescription = if (authCodeVisible) "隐藏授权码" else "显示授权码"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF4EAD8), RoundedCornerShape(18.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "15 分钟自动检查",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF2E3A32)
                    )
                    Text(
                        text = "本轮仅测试邮箱连接，暂不执行自动检查。",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6F7A70)
                    )
                }
                Switch(
                    checked = autoCheckEnabled,
                    onCheckedChange = { autoCheckEnabled = it }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        if (!store.isSecureStorageReady()) {
                            statusMessage = "加密存储不可用，未保存配置"
                            return@Button
                        }

                        val now = System.currentTimeMillis()
                        val success = store.save(
                            MailSettings(
                                email = email.trim(),
                                authCode = authCode,
                                autoCheckEnabled = autoCheckEnabled,
                                lastSavedAt = now
                            )
                        )

                        if (success) {
                            lastSavedAt = now
                            statusMessage = if (email.isNotBlank() && authCode.isNotBlank()) {
                                "设置已加密保存，已配置，尚未连接"
                            } else {
                                "设置已加密保存，但邮箱或授权码为空"
                            }
                        } else {
                            statusMessage = "加密保存失败"
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isTestingConnection
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Save,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("保存设置")
                }

                OutlinedButton(
                    onClick = {
                        val success = store.clear()
                        if (success || !store.isSecureStorageReady()) {
                            email = ""
                            authCode = ""
                            autoCheckEnabled = true
                            lastSavedAt = 0L
                            statusMessage = if (success) "配置已清除" else store.getStorageStatusText()
                        } else {
                            statusMessage = "清除配置失败"
                        }
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isTestingConnection
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("清除配置")
                }
            }

            Button(
                onClick = {
                    if (!store.isSecureStorageReady()) {
                        statusMessage = "加密存储不可用，无法测试邮箱连接"
                        return@Button
                    }

                    if (email.isBlank() || authCode.isBlank()) {
                        statusMessage = "邮箱或授权码为空"
                        return@Button
                    }

                    if (!email.trim().endsWith("@qq.com", ignoreCase = true)) {
                        statusMessage = "当前仅支持 QQ 邮箱"
                        return@Button
                    }

                    val now = System.currentTimeMillis()
                    val saved = store.save(
                        MailSettings(
                            email = email.trim(),
                            authCode = authCode,
                            autoCheckEnabled = autoCheckEnabled,
                            lastSavedAt = now
                        )
                    )

                    if (!saved) {
                        statusMessage = "加密保存失败，未开始连接测试"
                        return@Button
                    }

                    lastSavedAt = now
                    isTestingConnection = true
                    statusMessage = "正在测试邮箱连接..."

                    coroutineScope.launch {
                        val result = withContext(Dispatchers.IO) {
                            connectionTester.testConnection(
                                email = email.trim(),
                                authCode = authCode
                            )
                        }

                        isTestingConnection = false
                        statusMessage = result.message
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isTestingConnection
            ) {
                Icon(
                    imageVector = Icons.Rounded.MarkEmailRead,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isTestingConnection) "正在测试邮箱连接..." else "测试邮箱连接")
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { statusMessage = "本轮暂未接入自动检查和附件下载" },
                    modifier = Modifier.weight(1f),
                    enabled = !isTestingConnection
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("立即检查")
                }

                OutlinedButton(
                    onClick = { statusMessage = "本轮暂未接入 GitHub Release 更新" },
                    modifier = Modifier.weight(1f),
                    enabled = !isTestingConnection
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SystemUpdate,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("检查更新")
                }
            }

            StatusCard(
                title = "邮箱状态",
                value = statusMessage,
                note = "第四轮只测试 IMAP 登录和收件箱访问，不下载附件。"
            )

            StatusCard(
                title = "上次保存时间",
                value = formatSavedTime(lastSavedAt),
                note = "测试连接前会先保存当前邮箱配置。"
            )

            StatusCard(
                title = "当前版本",
                value = "1.0.0",
                note = "下一轮再接入 15 分钟自动检查。"
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "安全提示：测试连接只使用本机输入的 QQ 邮箱和授权码，不会读取正文、下载附件、删除或移动邮件。",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8A6D3B)
            )
        }
    }
}

@Composable
private fun StatusCard(
    title: String,
    value: String,
    note: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFFFFF), RoundedCornerShape(18.dp))
            .border(1.dp, Color(0xFFE7DCC7), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = Color(0xFF6F7A70)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF2E3A32)
        )
        Text(
            text = note,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6F7A70)
        )
    }
}

private fun formatSavedTime(timestamp: Long): String {
    if (timestamp <= 0L) return "暂无"
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}
