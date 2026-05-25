package com.postbird.frame.matepad

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MatePadPreviewHomeScreenV2() {
    val context = LocalContext.current
    var settingsOpen by remember { mutableStateOf(false) }
    var yearPickerOpen by remember { mutableStateOf(false) }
    var currentYear by remember { mutableIntStateOf(2026) }
    var refreshTick by remember { mutableIntStateOf(0) }
    var isPlaying by remember { mutableStateOf(true) }
    var selectedFilePath by remember { mutableStateOf<String?>(null) }
    val mediaFiles = remember(refreshTick) { MediaReceiveStore(context).listMediaFiles() }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000L)
            refreshTick++
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(6.dp)
    ) {
        ReceivedMediaFrame(
            modifier = Modifier
                .fillMaxSize()
                .alpha(if (yearPickerOpen) 0.72f else 1f),
            showCounter = false,
            isPlaying = isPlaying,
            selectedFilePath = selectedFilePath
        )

        AutoPlayLabel(
            isPlaying = isPlaying,
            onToggle = { isPlaying = !isPlaying },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 60.dp, top = 48.dp)
        )

        BottomPreviewControls(
            currentYear = currentYear,
            onYearClick = { yearPickerOpen = true },
            onMonthDayClick = { yearPickerOpen = true },
            onSettingsClick = { settingsOpen = !settingsOpen },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 42.dp, bottom = 36.dp)
                .zIndex(20f)
        )

        AnimatedVisibility(
            visible = settingsOpen,
            enter = slideInHorizontally(animationSpec = tween(280)) { it } + fadeIn(animationSpec = tween(220)),
            exit = slideOutHorizontally(animationSpec = tween(260)) { it } + fadeOut(animationSpec = tween(180)),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .zIndex(30f)
        ) {
            ScreenshotStyleSettingsPanel(
                mediaFiles = mediaFiles,
                currentYear = currentYear,
                selectedFilePath = selectedFilePath,
                onSelectMedia = { file -> selectedFilePath = file.absolutePath },
                onClose = { settingsOpen = false },
                onOpenYearPicker = { yearPickerOpen = true }
            )
        }

        AnimatedVisibility(
            visible = yearPickerOpen,
            enter = fadeIn(animationSpec = tween(180)) + scaleIn(animationSpec = tween(260), initialScale = 0.92f),
            exit = fadeOut(animationSpec = tween(180)) + scaleOut(animationSpec = tween(200), targetScale = 0.94f),
            modifier = Modifier
                .fillMaxSize()
                .zIndex(50f)
        ) {
            ScreenshotStyleYearPicker(
                currentYear = currentYear,
                onClose = { yearPickerOpen = false },
                onConfirm = {
                    currentYear = it
                    yearPickerOpen = false
                }
            )
        }
    }
}

@Composable
private fun AutoPlayLabel(
    isPlaying: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onToggle)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
            null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
        Text(if (isPlaying) "自动播放中" else "已暂停", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BottomPreviewControls(
    currentYear: Int,
    onYearClick: () -> Unit,
    onMonthDayClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.Bottom) {
        DarkCircleButton(onClick = onYearClick) {
            Text(currentYear.toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
        DarkCircleButton(onClick = onMonthDayClick) {
            Text("1 / 6", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
        DarkCircleButton(onClick = onSettingsClick) {
            Icon(Icons.Rounded.Settings, null, tint = Color.White, modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun DarkCircleButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(82.dp)
            .clip(CircleShape)
            .background(Color(0xFF101A24).copy(alpha = 0.96f))
            .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun ScreenshotStyleSettingsPanel(
    mediaFiles: List<File>,
    currentYear: Int,
    selectedFilePath: String?,
    onSelectMedia: (File) -> Unit,
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
    var showMailSettings by remember { mutableStateOf(false) }

    fun toast(message: String) {
        status = message
        Toast.makeText(context.applicationContext, message, Toast.LENGTH_LONG).show()
    }

    Box(
        modifier = Modifier
            .padding(end = 16.dp, top = 8.dp, bottom = 8.dp)
            .width(470.dp)
            .fillMaxHeight()
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFFEFF8F4).copy(alpha = 0.96f))
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                TopPill("邮箱设置", Icons.Rounded.Mail, Modifier.weight(1f)) { showMailSettings = !showMailSettings }
                TopPill("手动收件", Icons.Rounded.Download, Modifier.weight(1f)) { context.startActivity(Intent(context, ReceiveMediaActivity::class.java)) }
                TopPill("版本更新", Icons.Rounded.Refresh, Modifier.weight(1f)) { context.startActivity(Intent(context, UpdateActivity::class.java)) }
                TopPill("关闭", Icons.Rounded.Close, Modifier.weight(1f), onClick = onClose)
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Text("版本 ${readVersionName(context)}", color = Color(0xFF33423C), fontSize = 14.sp)
                Text(status, color = Color(0xFF00796B), fontSize = 14.sp)
            }

            AnimatedVisibility(
                visible = showMailSettings,
                enter = fadeIn(animationSpec = tween(180)) + slideInVertically(animationSpec = tween(220)) { -18 },
                exit = fadeOut(animationSpec = tween(160)) + slideOutVertically(animationSpec = tween(180)) { -18 }
            ) {
                CompactMailBox(
                    email = email,
                    onEmailChange = { email = it },
                    authCode = authCode,
                    onAuthCodeChange = { authCode = it },
                    authVisible = authVisible,
                    onToggleAuthVisible = { authVisible = !authVisible },
                    autoCheck = autoCheck,
                    onAutoCheckChange = { autoCheck = it },
                    testing = testing,
                    onSave = {
                        val ok = store.save(MailSettings(email.trim(), authCode, autoCheck, System.currentTimeMillis()))
                        toast(if (ok) "邮箱设置已保存" else "邮箱设置保存失败")
                    },
                    onTest = {
                        if (email.isBlank() || authCode.isBlank()) {
                            toast("邮箱或授权码为空")
                            return@CompactMailBox
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .clickable(onClick = onOpenYearPicker),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("播放年份", color = Color(0xFF263532), fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                YearChip(currentYear.toString())
                Text("-", color = Color(0xFF52625E), modifier = Modifier.padding(horizontal = 14.dp))
                YearChip(currentYear.toString())
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(18.dp).background(Color(0xFF168C86), CircleShape))
                Text("$currentYear-1", color = Color(0xFF102427), fontSize = 30.sp, fontWeight = FontWeight.Bold)
            }

            if (mediaFiles.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    Text("暂无素材", color = Color(0xFF60726E))
                }
            } else {
                LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(mediaFiles.take(20)) { file -> PreviewThumb(file = file, selected = file.absolutePath == selectedFilePath, onClick = { onSelectMedia(file) }) }
                }
            }
        }
    }
}

@Composable
private fun TopPill(text: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(Color(0xFF006D62))
            .clickable(onClick = onClick)
            .padding(horizontal = 9.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun YearChip(text: String) {
    Box(modifier = Modifier.clip(RoundedCornerShape(999.dp)).background(Color(0xFFE8F0EC)).padding(horizontal = 32.dp, vertical = 10.dp)) {
        Text(text, color = Color(0xFF263532), fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun CompactMailBox(
    email: String,
    onEmailChange: (String) -> Unit,
    authCode: String,
    onAuthCodeChange: (String) -> Unit,
    authVisible: Boolean,
    onToggleAuthVisible: () -> Unit,
    autoCheck: Boolean,
    onAutoCheckChange: (Boolean) -> Unit,
    testing: Boolean,
    onSave: () -> Unit,
    onTest: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.82f)).padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(value = email, onValueChange = onEmailChange, label = { Text("QQ 邮箱") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = authCode,
            onValueChange = onAuthCodeChange,
            label = { Text("授权码") },
            singleLine = true,
            visualTransformation = if (authVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = { IconButton(onClick = onToggleAuthVisible) { Icon(if (authVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null) } },
            modifier = Modifier.fillMaxWidth()
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("每 15 分钟自动检查", color = Color(0xFF33423C), modifier = Modifier.weight(1f))
            Switch(checked = autoCheck, onCheckedChange = onAutoCheckChange)
            OutlinedButton(onClick = onSave, modifier = Modifier.padding(start = 8.dp)) { Text("保存") }
            OutlinedButton(onClick = onTest, enabled = !testing, modifier = Modifier.padding(start = 8.dp)) { Text("测试") }
        }
    }
}

@Composable
private fun PreviewThumb(file: File, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(if (selected) 2.dp else 0.dp, if (selected) Color(0xFF168C86) else Color.Transparent, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        val lower = file.name.lowercase()
        val isImage = listOf(".jpg", ".jpeg", ".png", ".webp").any { lower.endsWith(it) }
        if (isImage) {
            val bitmap = remember(file.absolutePath, file.lastModified()) { BitmapFactory.decodeFile(file.absolutePath) }
            if (bitmap != null) Image(bitmap.asImageBitmap(), null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
        } else {
            Box(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)).background(Color(0xFF7AA2B8)), contentAlignment = Alignment.Center) { Text("视频", color = Color.White) }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun ScreenshotStyleYearPicker(currentYear: Int, onClose: () -> Unit, onConfirm: (Int) -> Unit) {
    var year by remember { mutableIntStateOf(currentYear) }
    var dragTotal by remember { mutableFloatStateOf(0f) }
    fun step(delta: Int) { year = (year + delta).coerceIn(2000, 2035) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.30f))
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragStart = { dragTotal = 0f },
                    onVerticalDrag = { change, dragAmount -> change.consume(); dragTotal += dragAmount },
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
        Column(modifier = Modifier.clip(RoundedCornerShape(34.dp)).background(Color.White.copy(alpha = 0.18f)).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.width(250.dp)) {
                    Text("选择播放年份", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("上下滑动切换年份", color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp)
                }
                IconButton(onClick = onClose) { Icon(Icons.Rounded.Close, contentDescription = "关闭", tint = Color.White) }
            }
            YearPickerValue((year - 1).toString(), current = false)
            AnimatedContent(
                targetState = year,
                transitionSpec = {
                    (slideInVertically(animationSpec = tween(220)) { 18 } + fadeIn(animationSpec = tween(220))) togetherWith
                        (slideOutVertically(animationSpec = tween(180)) { -18 } + fadeOut(animationSpec = tween(180)))
                },
                label = "yearValue"
            ) { value ->
                Box(modifier = Modifier.clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.18f)).padding(horizontal = 34.dp, vertical = 14.dp)) {
                    Text(value.toString(), color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold)
                }
            }
            YearPickerValue((year + 1).toString(), current = false)
            Button(onClick = { onConfirm(year) }, modifier = Modifier.fillMaxWidth()) { Text("应用年份") }
        }
    }
}

@Composable
private fun YearPickerValue(text: String, current: Boolean) {
    Text(text, color = if (current) Color.White else Color.White.copy(alpha = 0.40f), fontSize = if (current) 36.sp else 22.sp, fontWeight = if (current) FontWeight.Bold else FontWeight.Normal)
}

private fun readVersionName(context: Context): String {
    return try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "未知"
    } catch (_: Exception) {
        "未知"
    }
}
