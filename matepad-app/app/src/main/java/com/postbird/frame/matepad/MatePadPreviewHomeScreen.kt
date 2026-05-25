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
        targetValue = if (panelOpen || yearPickerOpen) 0.84f else 1f,
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
            .background(Color.Black)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black)
        ) {
            PreviewPlaybackArea(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(dimAlpha)
            )

            PlaybackPill(modifier = Modifier.align(Alignment.TopStart).padding(start = 60.dp, top = 48.dp))

            PreviewCircleControls(
                currentYear = currentYear,
                onYearClick = { yearPickerOpen = true },
                onMonthDayClick = { yearPickerOpen = true },
                onSettingsClick = { panelOpen = !panelOpen },
                modifier = Modifier.align(Alignment.BottomStart).padding(start = 42.dp, bottom = 36.dp).zIndex(20f)
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
    Box(modifier = modifier.background(Color.Black), contentAlignment = Alignment.Center) {
        ReceivedMediaFrame(
            modifier = Modifier.fillMaxSize(),
            showCounter = false
        )
    }
}

@Composable
private fun PlaybackPill(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color.Transparent)
            .padding(horizontal = 0.dp, vertical = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Pause, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
        Text("自动播放中", color = Color.White, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
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
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.Bottom) {
        GlassCircleButton(onClick = onYearClick) {
            Text(currentYear.toString(), color = Color.White, textAlign = TextAlign.Center, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        }
        GlassCircleButton(onClick = onMonthDayClick) {
            Text("1 / 6", color = Color.White, textAlign = TextAlign.Center, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
        }
        GlassCircleButton(onClick = onSettingsClick) {
            Icon(Icons.Rounded.Settings, contentDescription = "设置", tint = Color.White, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun GlassCircleButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(82.dp)
            .clip(CircleShape)
            .background(Color(0xFF101A24).copy(alpha = 0.96f))
            .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
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
    var status by remember { mutableStateOf("更新检查失败") }
    var testing by remember { mutableStateOf(false) }

    fun toast(message: String) {
        status = message
        Toast.makeText(context.applicationContext, message, Toast.LENGTH_LONG).show()
    }

    Card(
        modifier = Modifier
            .padding(end = 16.dp)
            .width(470.dp)
            .fillMaxHeight()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF8F4).copy(alpha = 0.96f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TopPillButton("邮箱设置", Icons.Rounded.Mail, Modifier.weight(1f), onClick = {
                    val saved = store.save(MailSettings(email.trim(), authCode, autoCheck, System.currentTimeMillis()))
                    toast(if (saved) "邮箱设置已保存" else "邮箱设置保存失败")
                })
                TopPillButton("手动收件", Icons.Rounded.Download, Modifier.weight(1f), onClick = {
                    context.startActivity(Intent(context, ReceiveMediaActivity::class.java))
                })
                TopPillButton("版本更新", Icons.Rounded.Refresh, Modifier.weight(1f), onClick = {
                    context.startActivity(Intent(context, UpdateActivity::class.java))
                })
                TopPillButton("关闭", Icons.Rounded.Close, Modifier.weight(1f), onClick = onClose)
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Text("版本 0.1.7", color = Color(0xFF33423C))
                Text(status, color = Color(0xFF00796B))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("播放年份", color = Color(0xFF263532), style = androidx.compose.material3.MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                YearChip(currentYear.toString())
                Text("-", color = Color(0xFF52625E), modifier = Modifier.padding(horizontal = 14.dp))
                YearChip(currentYear.toString())
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(18.dp).background(Color(0xFF168C86), CircleShape))
                Text("$currentYear-1", color = Color(0xFF102427), style = androidx.compose.material3.MaterialTheme.typography.headlineSmall)
            }

            if (mediaFiles.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("暂无素材", color = Color(0xFF60726E))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(mediaFiles.take(20)) { file -> PreviewThumb(file) }
                }
            }

            CompactMailSettings(
                email = email,
                onEmailChange = { email = it },
                authCode = authCode,
                onAuthCodeChange = { authCode = it },
                authVisible = authVisible,
                onAuthVisibleChange = { authVisible = !authVisible },
                autoCheck = autoCheck,
                onAutoCheckChange = { autoCheck = it },
                testing = testing,
                onTest = {
                    if (email.isBlank() || authCode.isBlank()) {
                        toast("邮箱或授权码为空")
                        return@CompactMailSettings
                    }
                    store.save(MailSettings(email.trim(), authCode, autoCheck, System.currentTimeMillis()))
                    testing = true
                    toast("正在测试邮箱连接...")
                    scope.launch {
                        val result = withContext(Dispatchers.IO) { tester.testConnection(email.trim(), authCode) }
                        testing = false
                        toast(result.message)
                    }
                }
            )
        }
    }
}

@Composable
private fun TopPillButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(999.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(text)
    }
}

@Composable
private fun YearChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFFE8F0EC))
            .padding(horizontal = 32.dp, vertical = 10.dp)
    ) {
        Text(text, color = Color(0xFF263532), style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun CompactMailSettings(
    email: String,
    onEmailChange: (String) -> Unit,
    authCode: String,
    onAuthCodeChange: (String) -> Unit,
    authVisible: Boolean,
    onAuthVisibleChange: () -> Unit,
    autoCheck: Boolean,
    onAutoCheckChange: (Boolean) -> Unit,
    testing: Boolean,
    onTest: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(value = email, onValueChange = onEmailChange, label = { Text("QQ 邮箱") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = authCode,
            onValueChange = onAuthCodeChange,
            label = { Text("授权码") },
            singleLine = true,
            visualTransformation = if (authVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = onAuthVisibleChange) {
                    Icon(if (authVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null)
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("每 15 分钟自动检查", color = Color(0xFF33423C), modifier = Modifier.weight(1f))
            Switch(checked = autoCheck, onCheckedChange = onAutoCheckChange)
            OutlinedButton(onClick = onTest, enabled = !testing, modifier = Modifier.padding(start = 8.dp)) { Text("测试邮箱") }
        }
    }
}

@Composable
private fun PreviewThumb(file: File) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(6.dp)
    ) {
        val lower = file.name.lowercase()
        val isImage = listOf(".jpg", ".jpeg", ".png", ".webp").any { lower.endsWith(it) }
        if (isImage) {
            val bitmap = remember(file.absolutePath, file.lastModified()) { BitmapFactory.decodeFile(file.absolutePath) }
            if (bitmap != null) Image(bitmap.asImageBitmap(), null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
        } else {
            Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)).background(Color(0xFF7AA2B8)), contentAlignment = Alignment.Center) {
                Text("视频", color = Color.White)
            }
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
            .background(Color.Black.copy(alpha = 0.30f))
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
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.18f)),
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
