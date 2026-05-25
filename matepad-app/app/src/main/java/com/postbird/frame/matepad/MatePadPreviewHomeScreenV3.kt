package com.postbird.frame.matepad

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.postbird.frame.matepad.mail.MediaReceiveStore
import com.postbird.frame.matepad.mail.ReceiveMediaActivity
import com.postbird.frame.matepad.update.UpdateActivity
import java.io.File
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MatePadPreviewHomeScreenV3() {
    val context = LocalContext.current
    var settingsOpen by remember { mutableStateOf(false) }
    var pickerOpen by remember { mutableStateOf(false) }
    var year by remember { mutableIntStateOf(2026) }
    var playing by remember { mutableStateOf(true) }
    var tick by remember { mutableIntStateOf(0) }
    var selectedPath by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf("更新检查失败") }
    val files = remember(tick) { MediaReceiveStore(context).listMediaFiles() }

    LaunchedEffect(Unit) {
        while (true) {
            delay(5000L)
            tick++
        }
    }

    Box(Modifier.fillMaxSize().background(Color.Black).padding(6.dp)) {
        ReceivedMediaFrame(
            modifier = Modifier.fillMaxSize().alpha(if (pickerOpen) 0.72f else 1f),
            showCounter = false,
            isPlaying = playing,
            selectedFilePath = selectedPath
        )

        AutoPlayPillV3(
            playing = playing,
            onClick = { playing = !playing },
            modifier = Modifier.align(Alignment.TopStart).padding(start = 60.dp, top = 48.dp)
        )

        BottomControlsV3(
            year = year,
            onYear = { pickerOpen = true },
            onDay = { pickerOpen = true },
            onSettings = { settingsOpen = !settingsOpen },
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 42.dp, bottom = 36.dp).zIndex(20f)
        )

        AnimatedVisibility(
            visible = settingsOpen,
            enter = slideInHorizontally(tween(280)) { it } + fadeIn(tween(220)),
            exit = slideOutHorizontally(tween(260)) { it } + fadeOut(tween(180)),
            modifier = Modifier.align(Alignment.CenterEnd).zIndex(30f)
        ) {
            SettingsPanelV3(
                files = files,
                year = year,
                selectedPath = selectedPath,
                status = status,
                onStatus = { status = it },
                onSelect = { selectedPath = it.absolutePath },
                onOpenYear = { pickerOpen = true },
                onClose = { settingsOpen = false }
            )
        }

        AnimatedVisibility(
            visible = pickerOpen,
            enter = fadeIn(tween(180)) + scaleIn(tween(260), initialScale = 0.92f),
            exit = fadeOut(tween(180)) + scaleOut(tween(200), targetScale = 0.94f),
            modifier = Modifier.fillMaxSize().zIndex(50f)
        ) {
            YearPickerV3(currentYear = year, onClose = { pickerOpen = false }) {
                year = it
                pickerOpen = false
            }
        }
    }
}

@Composable
private fun AutoPlayPillV3(playing: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.clip(RoundedCornerShape(999.dp)).clickable(onClick = onClick).padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, tint = Color.White, modifier = Modifier.size(22.dp))
        Text(if (playing) "自动播放中" else "已暂停", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun BottomControlsV3(year: Int, onYear: () -> Unit, onDay: () -> Unit, onSettings: () -> Unit, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(18.dp), verticalAlignment = Alignment.Bottom) {
        DarkCircleV3(onClick = onYear) { Text(year.toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
        DarkCircleV3(onClick = onDay) { Text("1 / 6", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) }
        DarkCircleV3(onClick = onSettings) { Icon(Icons.Rounded.Settings, null, tint = Color.White, modifier = Modifier.size(32.dp)) }
    }
}

@Composable
private fun DarkCircleV3(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.size(82.dp).clip(CircleShape).background(Color(0xFF101A24).copy(alpha = 0.96f)).border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
private fun SettingsPanelV3(
    files: List<File>,
    year: Int,
    selectedPath: String?,
    status: String,
    onStatus: (String) -> Unit,
    onSelect: (File) -> Unit,
    onOpenYear: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier.padding(end = 14.dp, top = 10.dp, bottom = 10.dp).fillMaxWidth(0.36f).widthIn(min = 360.dp, max = 470.dp).fillMaxHeight().clip(RoundedCornerShape(22.dp)).background(Color(0xFFEFF8F4).copy(alpha = 0.96f))
    ) {
        Box(Modifier.align(Alignment.CenterStart).fillMaxHeight().width(2.dp).background(Color(0xFF5AA59E).copy(alpha = 0.62f)))
        Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                TopButtonV3("邮箱设置", Icons.Rounded.Mail, Modifier.weight(1f)) { onStatus("邮箱设置已收起，后续进入二级卡片") }
                TopButtonV3("手动收件", Icons.Rounded.Download, Modifier.weight(1f)) { context.startActivity(Intent(context, ReceiveMediaActivity::class.java)) }
                TopButtonV3("版本更新", Icons.Rounded.Refresh, Modifier.weight(1f)) { context.startActivity(Intent(context, UpdateActivity::class.java)) }
                TopButtonV3("关闭", Icons.Rounded.Close, Modifier.weight(1f), onClick = onClose)
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                Text("版本 ${readVersionNameV3(context)}", color = Color(0xFF33423C), fontSize = 13.sp)
                Text(status, color = Color(0xFF00796B), fontSize = 13.sp)
            }

            Row(
                modifier = Modifier.fillMaxWidth().height(76.dp).clip(RoundedCornerShape(22.dp)).background(Color.White).clickable(onClick = onOpenYear).padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("播放年份", color = Color(0xFF263532), fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                YearChipV3(year.toString())
                Text("-", color = Color(0xFF52625E), modifier = Modifier.padding(horizontal = 12.dp))
                YearChipV3(year.toString())
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.size(18.dp).background(Color(0xFF168C86), CircleShape))
                Text("$year-1", color = Color(0xFF102427), fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }

            if (files.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) { Text("暂无素材", color = Color(0xFF60726E), fontSize = 14.sp) }
            } else {
                LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(files.take(20)) { file -> ThumbV3(file, selected = file.absolutePath == selectedPath) { onSelect(file) } }
                }
            }
        }
    }
}

@Composable
private fun TopButtonV3(text: String, icon: ImageVector, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier.height(46.dp).clip(RoundedCornerShape(999.dp)).background(Color(0xFF006D62)).clickable(onClick = onClick).padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.White, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(3.dp))
        Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun YearChipV3(text: String) {
    Box(Modifier.clip(RoundedCornerShape(999.dp)).background(Color(0xFFE8F0EC)).padding(horizontal = 28.dp, vertical = 10.dp)) {
        Text(text, color = Color(0xFF263532), fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ThumbV3(file: File, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().height(70.dp).clip(RoundedCornerShape(12.dp)).background(Color.White).border(if (selected) 2.dp else 0.dp, if (selected) Color(0xFF168C86) else Color.Transparent, RoundedCornerShape(12.dp)).clickable(onClick = onClick).padding(6.dp)
    ) {
        val lower = file.name.lowercase()
        val isImage = listOf(".jpg", ".jpeg", ".png", ".webp").any { lower.endsWith(it) }
        if (isImage) {
            val bitmap = remember(file.absolutePath, file.lastModified()) { BitmapFactory.decodeFile(file.absolutePath) }
            if (bitmap != null) Image(bitmap.asImageBitmap(), null, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
        } else {
            Box(Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)).background(Color(0xFF7AA2B8)), contentAlignment = Alignment.Center) { Text("视频", color = Color.White) }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun YearPickerV3(currentYear: Int, onClose: () -> Unit, onConfirm: (Int) -> Unit) {
    var year by remember { mutableIntStateOf(currentYear) }
    var dragTotal by remember { mutableFloatStateOf(0f) }
    fun step(delta: Int) { year = (year + delta).coerceIn(2000, 2035) }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.30f)).pointerInput(Unit) {
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
        Column(Modifier.clip(RoundedCornerShape(34.dp)).background(Color.White.copy(alpha = 0.18f)).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.width(250.dp)) {
                    Text("选择播放年份", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("上下滑动切换年份", color = Color.White.copy(alpha = 0.55f), fontSize = 12.sp)
                }
                Text("×", color = Color.White, fontSize = 28.sp, modifier = Modifier.clickable(onClick = onClose).padding(8.dp))
            }
            Text((year - 1).toString(), color = Color.White.copy(alpha = 0.40f), fontSize = 22.sp)
            AnimatedContent(targetState = year, transitionSpec = { (slideInVertically(tween(220)) { 18 } + fadeIn(tween(220))) togetherWith (slideOutVertically(tween(180)) { -18 } + fadeOut(tween(180))) }, label = "yearValue") { value ->
                Box(Modifier.clip(RoundedCornerShape(18.dp)).background(Color.White.copy(alpha = 0.18f)).padding(horizontal = 34.dp, vertical = 14.dp)) { Text(value.toString(), color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Bold) }
            }
            Text((year + 1).toString(), color = Color.White.copy(alpha = 0.40f), fontSize = 22.sp)
            Button(onClick = { onConfirm(year) }, modifier = Modifier.fillMaxWidth()) { Text("应用年份") }
        }
    }
}

private fun readVersionNameV3(context: Context): String {
    return try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "未知"
    } catch (_: Exception) {
        "未知"
    }
}
