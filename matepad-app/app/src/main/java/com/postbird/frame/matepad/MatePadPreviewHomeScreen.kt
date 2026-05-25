package com.postbird.frame.matepad

import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.postbird.frame.matepad.mail.MediaReceiveStore
import com.postbird.frame.matepad.mail.QqMailConnectionTester
import com.postbird.frame.matepad.mail.ReceiveMediaActivity
import com.postbird.frame.matepad.settings.MailSettings
import com.postbird.frame.matepad.settings.MailSettingsStore
import com.postbird.frame.matepad.update.UpdateActivity
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MatePadPreviewHomeScreen() {
    val context = LocalContext.current
    var panelOpen by remember { mutableStateOf(false) }
    var yearPickerOpen by remember { mutableStateOf(false) }
    var currentYear by remember { mutableIntStateOf(2026) }
    var tick by remember { mutableIntStateOf(0) }
    val dimAlpha by animateFloatAsState(
        targetValue = if (panelOpen || yearPickerOpen) 0.78f else 1f,
        label = "previewDimAlpha"
    )
    val mediaFiles = remember(tick) { MediaReceiveStore(context).listMediaFiles() }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000L)
            tick++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050607))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(44.dp))
                .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(44.dp))
                .background(Color(0xFF0D0F12))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(dimAlpha)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                listOf(Color.White.copy(alpha = 0.06f), Color.Transparent),
                                radius = 900f
                            )
                        )
                )
                PreviewPlaybackArea(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(if (panelOpen) 0.66f else 1f)
                )
            }

            PlaybackPill(modifier = Modifier.align(Alignment.TopStart).padding(20.dp))

            PreviewCircleControls(
                currentYear = currentYear,
                onYearClick = { yearPickerOpen = true },
                onMonthDayClick = { yearPickerOpen = true },
                onSettingsClick = { panelOpen = !panelOpen },
                modifier = Modifier.align(Alignment.BottomStart).padding(20.dp).zIndex(20f)
            )

            AnimatedVisibility(
                visible = panelOpen,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.align(Alignment.CenterEnd).zIndex(30f)
            ) {
                PreviewSettingsPanel(
                    mediaFiles = mediaFiles,
                    currentYear = currentYear,
                    onClose = { panelOpen = false },
                    onOpenYearPicker = { yearPickerOpen = true }
                )
            }

            AnimatedVisibility(
                visible = yearPickerOpen,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize().zIndex(50f)
            ) {
                PreviewYearPicker(
                    currentYear = currentYear,
                    onClose = { yearPickerOpen = false },
                    onConfirm = { year ->
                        currentYear = year
                        yearPickerOpen = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PreviewPlaybackArea(modifier: Modifier = Modifier) {
    Box(modifier = modifier.background(Color.Black)) {
        ReceivedMediaFrame(
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun PlaybackPill(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
            .padding(horizontal = 14.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(Color(0xFF6EE7B7), CircleShape)
        )
        Text("自动播放中", color = Color.White.copy(alpha = 0.78f))
        Icon(Icons.Rounded.Pause, contentDescription = null, tint = Color.White.copy(alpha = 0.45f), modifier = Modifier.size(14.dp))
    }
}

@Composable
private fun PreviewCircleControls(
    currentYear: Int,
    onYearClick: () -> Unit,
    onMonthDayClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Bottom) {
        GlassCircleButton(onClick = onYearClick) {
            Text(currentYear.toString(), color = Color.White, textAlign = TextAlign.Center)
            Text("年", color = Color.White.copy(alpha = 0.65f), textAlign = TextAlign.Center)
        }
        GlassCircleButton(onClick = onMonthDayClick) {
            Text("05", color = Color.White, textAlign = TextAlign.Center)
            Text("/25", color = Color.White.copy(alpha = 0.70f), textAlign = TextAlign.Center)
        }
        GlassCircleButton(onClick = onSettingsClick) {
            Icon(Icons.Rounded.Settings, contentDescription = "设置", tint = Color.White, modifier = Modifier.size(26.dp))
        }
    }
}

@Composable
private fun GlassCircleButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.12f))
            .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

@Composable
private fun PreviewSettingsPanel(
    mediaFiles: List<File>,
    currentYear: Int,
    onClose: () -> Unit,
    onOpenYearPicker: () -> Unit
) {
    val context = LocalContext.current
    val store = remember(context) { MailSettingsStore(context) }
    val tester = remember { QqMailConnectionTester() }
    val scope = rememberCoroutineScope()
    val initialSettings = remember { store.load() }
    var email by remember { mutableStateOf(initialSettings.email) }
    var authCode by remember { mutableStateOf(initialSettings.authCode) }
    var authVisible by remember { mutableStateOf(false) }
    var autoCheck by remember { mutableStateOf(initialSettings.autoCheckEnabled) }
    var status by remember { mutableStateOf("等待自动检查邮箱") }
    var testing by remember { mutableStateOf(false) }

    fun toast(message: String) {
        status = message
        Toast.makeText(context.applicationContext, message, Toast.LENGTH_LONG).show()
    }

    Card(
        modifier = Modifier
            .padding(end = 20.dp)
            .width(350.dp)
            .fillMaxHeight()
            .padding(vertical = 20.dp),
        shape = RoundedCornerShape(34.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xB8101317)),
        elevation = CardDefaults.cardElevation(defaultElevation = 18.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.dp, Color.Transparent)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("相框设置", color = Color.White)
                    Text("邮箱收取 · 自动播放 · 更新", color = Color.White.copy(alpha = 0.55f))
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Rounded.Close, contentDescription = "关闭", tint = Color.White)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SettingsGlassSection(title = "QQ 邮箱接收", icon = { Icon(Icons.Rounded.Mail, null, tint = Color.White, modifier = Modifier.size(16.dp)) }) {
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("QQ 邮箱") }, singleLine = true)
                    OutlinedTextField(
                        value = authCode,
                        onValueChange = { authCode = it },
                        label = { Text("授权码") },
                        singleLine = true,
                        visualTransformation = if (authVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { authVisible = !authVisible }) {
                                Icon(if (authVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null)
                            }
                        }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val saved = store.save(MailSettings(email.trim(), authCode, autoCheck, System.currentTimeMillis()))
                                toast(if (saved) "设置已保存" else "设置保存失败")
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("保存") }
                        OutlinedButton(
                            onClick = {
                                if (email.isBlank() || authCode.isBlank()) {
                                    toast("邮箱或授权码为空")
                                    return@OutlinedButton
                                }
                                store.save(MailSettings(email.trim(), authCode, autoCheck, System.currentTimeMillis()))
                                testing = true
                                toast("正在测试邮箱连接...")
                                scope.launch {
                                    val result = withContext(Dispatchers.IO) { tester.testConnection(email.trim(), authCode) }
                                    testing = false
                                    toast(result.message)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !testing
                        ) { Text("测试") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { context.startActivity(Intent(context, ReceiveMediaActivity::class.java)) },
                            modifier = Modifier.weight(1f)
                        ) { Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(6.dp)); Text("立即检查") }
                        OutlinedButton(
                            onClick = { context.startActivity(Intent(context, UpdateActivity::class.java)) },
                            modifier = Modifier.weight(1f)
                        ) { Icon(Icons.Rounded.Download, null, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(6.dp)); Text("检查更新") }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("每 15 分钟自动检查", color = Color.White.copy(alpha = 0.72f), modifier = Modifier.weight(1f))
                        Switch(checked = autoCheck, onCheckedChange = { autoCheck = it })
                    }
                }

                SettingsGlassSection(title = "播放控制") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SmallGlassButton("顺序播放", Modifier.weight(1f))
                        SmallGlassButton("8 秒切换", Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SmallGlassButton("年份 $currentYear", Modifier.weight(1f), onClick = onOpenYearPicker)
                        SmallGlassButton("全部日期", Modifier.weight(1f))
                    }
                }

                SettingsGlassSection(title = "照片列表") {
                    if (mediaFiles.isEmpty()) {
                        Text("暂无素材", color = Color.White.copy(alpha = 0.58f))
                    } else {
                        LazyVerticalGrid(columns = GridCells.Fixed(3), modifier = Modifier.height(250.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(mediaFiles.take(12)) { file -> PreviewThumb(file) }
                        }
                    }
                }
            }

            Text(
                text = status,
                color = Color.White.copy(alpha = 0.62f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            )
        }
    }
}

@Composable
private fun SettingsGlassSection(title: String, icon: @Composable (() -> Unit)? = null, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(24.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            icon?.invoke()
            Text(title, color = Color.White)
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            content()
        }
    }
}

@Composable
private fun SmallGlassButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    OutlinedButton(onClick = onClick, modifier = modifier) { Text(text) }
}

@Composable
private fun PreviewThumb(file: File) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(82.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
    ) {
        val lower = file.name.lowercase()
        val isImage = listOf(".jpg", ".jpeg", ".png", ".webp").any { lower.endsWith(it) }
        if (isImage) {
            val bitmap = remember(file.absolutePath, file.lastModified()) { BitmapFactory.decodeFile(file.absolutePath) }
            if (bitmap != null) Image(bitmap.asImageBitmap(), null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Text("视频", color = Color.White.copy(alpha = 0.66f), modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun PreviewYearPicker(currentYear: Int, onClose: () -> Unit, onConfirm: (Int) -> Unit) {
    var year by remember { mutableIntStateOf(currentYear) }
    var dragTotal by remember { mutableFloatStateOf(0f) }

    fun step(delta: Int) {
        year = (year + delta).coerceIn(2000, 2035)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.28f))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragTotal = 0f },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        dragTotal += dragAmount
                    },
                    onDragEnd = {
                        when {
                            dragTotal < -20f -> step(1)
                            dragTotal > 20f -> step(-1)
                        }
                        dragTotal = 0f
                    },
                    onDragCancel = { dragTotal = 0f }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.widthIn(min = 330.dp, max = 360.dp),
            shape = RoundedCornerShape(34.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.16f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 18.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("选择播放年份", color = Color.White)
                        Text("上下滑动切换年份", color = Color.White.copy(alpha = 0.55f))
                    }
                    IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, contentDescription = "关闭", tint = Color.White) }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text((year - 1).toString(), color = Color.White.copy(alpha = 0.40f))
                    Box(modifier = Modifier.clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.18f)).padding(horizontal = 34.dp, vertical = 14.dp)) {
                        Text(year.toString(), color = Color.White)
                    }
                    Text((year + 1).toString(), color = Color.White.copy(alpha = 0.40f))
                }
                Button(onClick = { onConfirm(year) }, modifier = Modifier.fillMaxWidth()) { Text("应用年份") }
            }
        }
    }
}
