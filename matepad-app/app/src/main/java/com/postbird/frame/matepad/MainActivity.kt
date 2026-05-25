package com.postbird.frame.matepad

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.postbird.frame.matepad.mail.QqMailConnectionTester
import com.postbird.frame.matepad.mail.ReceiveMediaActivity
import com.postbird.frame.matepad.settings.MailSettings
import com.postbird.frame.matepad.settings.MailSettingsStore
import com.postbird.frame.matepad.update.UpdateActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PostBirdFrameTheme { MatePadFrameScreen() } }
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
    var selectorVisible by remember { mutableStateOf(false) }
    var selectorMode by remember { mutableStateOf("年份") }
    val contentAlpha by animateFloatAsState(
        targetValue = if (settingsVisible || selectorVisible) 0.72f else 1f,
        label = "contentAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFE6F5F0), Color(0xFFFFF4D8), Color(0xFFEAF3DF))
                )
            )
    ) {
        FrameMainContent(
            modifier = Modifier.fillMaxSize().alpha(contentAlpha)
        )

        PlaybackStatusPill(
            modifier = Modifier.align(Alignment.TopStart).padding(start = 32.dp, top = 28.dp)
        )

        AnimatedVisibility(
            visible = selectorVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize().zIndex(2f)
        ) {
            DateSelectorOverlay(
                mode = selectorMode,
                onClose = { selectorVisible = false }
            )
        }

        AnimatedVisibility(
            visible = settingsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize().zIndex(3f)
        ) { SettingsOverlay(onClose = { settingsVisible = false }) }

        BottomLeftControls(
            settingsVisible = settingsVisible,
            onYearClick = {
                if (selectorVisible && selectorMode == "年份") {
                    selectorVisible = false
                } else {
                    selectorMode = "年份"
                    selectorVisible = true
                }
                settingsVisible = false
            },
            onMonthDayClick = {
                if (selectorVisible && selectorMode == "月日") {
                    selectorVisible = false
                } else {
                    selectorMode = "月日"
                    selectorVisible = true
                }
                settingsVisible = false
            },
            onSettingsClick = {
                settingsVisible = !settingsVisible
                selectorVisible = false
            },
            modifier = Modifier.align(Alignment.BottomStart).padding(28.dp).zIndex(4f)
        )
    }
}

@Composable
private fun FrameMainContent(modifier: Modifier) {
    Box(modifier = modifier) {
        ReceivedMediaFrame(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.76f)
                .aspectRatio(16f / 10f)
        )
    }
}

@Composable
private fun PlaybackStatusPill(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.76f), RoundedCornerShape(24.dp))
            .border(1.dp, Color.White.copy(alpha = 0.62f), RoundedCornerShape(24.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(8.dp).background(Color(0xFF6E8F7C), CircleShape))
        Text("自动播放中", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF2E3A32))
    }
}

@Composable
private fun BottomLeftControls(
    settingsVisible: Boolean,
    onYearClick: () -> Unit,
    onMonthDayClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        YearCircleButton(onClick = onYearClick)
        MonthDayCircleButton(onClick = onMonthDayClick)
        FloatingActionButton(
            onClick = onSettingsClick,
            containerColor = Color(0xFF6E8F7C),
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                imageVector = if (settingsVisible) Icons.Rounded.Close else Icons.Rounded.Settings,
                contentDescription = if (settingsVisible) "关闭设置" else "打开设置"
            )
        }
    }
}

@Composable
private fun YearCircleButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Color.White.copy(alpha = 0.86f),
        contentColor = Color(0xFF2E3A32),
        shape = CircleShape,
        modifier = Modifier.size(56.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("2026", style = MaterialTheme.typography.labelMedium)
            Text("年", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun MonthDayCircleButton(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Color.White.copy(alpha = 0.86f),
        contentColor = Color(0xFF2E3A32),
        shape = CircleShape,
        modifier = Modifier.size(56.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("05", style = MaterialTheme.typography.labelMedium)
            Text("/25", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun DateSelectorOverlay(mode: String, onClose: () -> Unit) {
    var selectedValue by remember(mode) { mutableIntStateOf(if (mode == "年份") 2026 else 25) }
    var dragTotal by remember { mutableFloatStateOf(0f) }

    fun step(delta: Int) {
        selectedValue = if (mode == "年份") {
            (selectedValue + delta).coerceIn(1970, 2100)
        } else {
            (selectedValue + delta).coerceIn(1, 31)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6F2).copy(alpha = 0.38f))
            .pointerInput(mode) {
                detectVerticalDragGestures(
                    onDragStart = { dragTotal = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragTotal += dragAmount
                    },
                    onDragEnd = {
                        when {
                            dragTotal <= -80f -> step(1)
                            dragTotal >= 80f -> step(-1)
                        }
                        dragTotal = 0f
                    },
                    onDragCancel = { dragTotal = 0f }
                )
            }
            .padding(32.dp)
    ) {
        Card(
            modifier = Modifier.align(Alignment.Center).widthIn(min = 300.dp, max = 420.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.86f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 26.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("选择$mode", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF2E3A32))
                SelectorValueText(text = selectorLabel(mode, selectedValue - 1), current = false)
                SelectorValueText(text = selectorLabel(mode, selectedValue), current = true)
                SelectorValueText(text = selectorLabel(mode, selectedValue + 1), current = false)
                Text("上下滑动选择", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6F7A70), textAlign = TextAlign.Center)
                OutlinedButton(onClick = onClose) { Text("完成") }
            }
        }
    }
}

@Composable
private fun SelectorValueText(text: String, current: Boolean) {
    Text(
        text = text,
        style = if (current) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium,
        color = if (current) Color(0xFF2E3A32) else Color(0xFF8A9A8F)
    )
}

private fun selectorLabel(mode: String, value: Int): String {
    return if (mode == "年份") {
        "$value"
    } else {
        "05 / ${value.coerceIn(1, 31).toString().padStart(2, '0')}"
    }
}

@Composable
private fun PostBirdPlaceholder() {
    Box(modifier = Modifier.size(116.dp).background(Color(0xFFF7DFA2), CircleShape)) {
        Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 18.dp).size(58.dp).background(Color(0xFF6E8F7C), CircleShape))
        Row(modifier = Modifier.align(Alignment.TopCenter).padding(top = 35.dp), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            BirdEye()
            BirdEye()
        }
        Card(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp).width(72.dp).height(44.dp),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.White).border(1.dp, Color(0xFFE8B85F), RoundedCornerShape(8.dp))) {
                Text("✉", modifier = Modifier.align(Alignment.Center), color = Color(0xFFE8B85F))
            }
        }
    }
}

@Composable
private fun BirdEye() {
    Box(modifier = Modifier.size(12.dp).background(Color.White, CircleShape)) {
        Box(modifier = Modifier.align(Alignment.Center).size(5.dp).background(Color(0xFF2E3A32), CircleShape))
    }
}

@Composable
private fun SettingsOverlay(onClose: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.18f)).padding(28.dp)) {
        SettingsPanel(
            onClose = onClose,
            modifier = Modifier.align(Alignment.CenterEnd).widthIn(min = 456.dp, max = 540.dp).fillMaxHeight()
        )
    }
}

@Composable
private fun SettingsPanel(onClose: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val store = remember(context) { MailSettingsStore(context) }
    val connectionTester = remember { QqMailConnectionTester() }
    val coroutineScope = rememberCoroutineScope()
    val initialSettings = remember { store.load() }
    val scrollState = rememberScrollState()

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

    fun showStatus(message: String) {
        statusMessage = message
        Toast.makeText(context.applicationContext, message, Toast.LENGTH_LONG).show()
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8EC))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(26.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("设置", style = MaterialTheme.typography.headlineSmall, color = Color(0xFF2E3A32))
                    Text("邮箱配置、附件接收与联网更新", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6F7A70))
                }
                IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, contentDescription = "关闭设置") }
            }

            StatusCard("当前提示", statusMessage, "测试结果会显示在这里，同时弹出系统提示。")

            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("QQ 邮箱") }, placeholder = { Text("example@qq.com") }, singleLine = true, modifier = Modifier.fillMaxWidth())
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
                        Icon(if (authCodeVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth().background(Color(0xFFF4EAD8), RoundedCornerShape(18.dp)).padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("15 分钟自动检查", style = MaterialTheme.typography.titleMedium, color = Color(0xFF2E3A32))
                    Text("当前先支持手动立即检查，自动轮询后续接入。", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6F7A70))
                }
                Switch(checked = autoCheckEnabled, onCheckedChange = { autoCheckEnabled = it })
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        if (!store.isSecureStorageReady()) { showStatus("加密存储不可用，未保存配置"); return@Button }
                        val now = System.currentTimeMillis()
                        val success = store.save(MailSettings(email.trim(), authCode, autoCheckEnabled, now))
                        if (success) { lastSavedAt = now; showStatus("设置已加密保存") } else showStatus("加密保存失败")
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isTestingConnection
                ) { Icon(Icons.Rounded.Save, null); Spacer(Modifier.width(8.dp)); Text("保存设置") }

                OutlinedButton(
                    onClick = {
                        val success = store.clear()
                        if (success || !store.isSecureStorageReady()) { email = ""; authCode = ""; autoCheckEnabled = true; lastSavedAt = 0L; showStatus(if (success) "配置已清除" else store.getStorageStatusText()) } else showStatus("清除配置失败")
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !isTestingConnection
                ) { Icon(Icons.Rounded.DeleteOutline, null); Spacer(Modifier.width(8.dp)); Text("清除配置") }
            }

            Button(
                onClick = {
                    if (!store.isSecureStorageReady()) { showStatus("加密存储不可用，无法测试邮箱连接"); return@Button }
                    if (email.isBlank() || authCode.isBlank()) { showStatus("邮箱或授权码为空"); return@Button }
                    if (!email.trim().endsWith("@qq.com", ignoreCase = true)) { showStatus("当前仅支持 QQ 邮箱"); return@Button }
                    val now = System.currentTimeMillis()
                    val saved = store.save(MailSettings(email.trim(), authCode, autoCheckEnabled, now))
                    if (!saved) { showStatus("加密保存失败，未开始连接测试"); return@Button }
                    lastSavedAt = now
                    isTestingConnection = true
                    showStatus("正在测试邮箱连接...")
                    coroutineScope.launch {
                        val result = withContext(Dispatchers.IO) { connectionTester.testConnection(email.trim(), authCode) }
                        isTestingConnection = false
                        showStatus(result.message)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isTestingConnection
            ) { Icon(Icons.Rounded.MarkEmailRead, null); Spacer(Modifier.width(8.dp)); Text(if (isTestingConnection) "正在测试邮箱连接..." else "测试邮箱连接") }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { context.startActivity(Intent(context, ReceiveMediaActivity::class.java)) },
                    modifier = Modifier.weight(1f),
                    enabled = !isTestingConnection
                ) { Icon(Icons.Rounded.Refresh, null); Spacer(Modifier.width(8.dp)); Text("立即检查") }

                OutlinedButton(
                    onClick = { context.startActivity(Intent(context, UpdateActivity::class.java)) },
                    modifier = Modifier.weight(1f),
                    enabled = !isTestingConnection
                ) { Icon(Icons.Rounded.SystemUpdate, null); Spacer(Modifier.width(8.dp)); Text("检查更新") }
            }

            StatusCard("邮箱状态", statusMessage, "当前支持 IMAP 连接测试、手动收取照片/视频附件。")
            StatusCard("上次保存时间", formatSavedTime(lastSavedAt), "测试连接前会先保存当前邮箱配置。")
            StatusCard("当前版本", getAppVersionName(context), "QQ 邮箱接收与更新测试版。")

            Text(
                text = "安全提示：附件接收只保存照片和视频，不会删除、移动或标记邮件。",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF8A6D3B)
            )
        }
    }
}

@Composable
private fun StatusCard(title: String, value: String, note: String) {
    Column(
        modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(18.dp)).border(1.dp, Color(0xFFE7DCC7), RoundedCornerShape(18.dp)).padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = Color(0xFF6F7A70))
        Text(value, style = MaterialTheme.typography.titleMedium, color = Color(0xFF2E3A32))
        Text(note, style = MaterialTheme.typography.bodySmall, color = Color(0xFF6F7A70))
    }
}

private fun formatSavedTime(timestamp: Long): String {
    if (timestamp <= 0L) return "暂无"
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(timestamp))
}

private fun getAppVersionName(context: android.content.Context): String {
    return try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "未知"
    } catch (_: Exception) {
        "未知"
    }
}
