package com.postbird.frame.matepad

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.postbird.frame.matepad.mail.MediaReceiveStore
import java.io.File
import kotlinx.coroutines.delay

@Composable
fun ReceivedMediaFrame(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var refreshTick by remember { mutableStateOf(0) }
    var playIndex by remember { mutableStateOf(0) }
    val mediaFiles = remember(refreshTick) { MediaReceiveStore(context).listMediaFiles() }

    LaunchedEffect(Unit) {
        while (true) {
            delay(MEDIA_REFRESH_INTERVAL_MS)
            refreshTick++
        }
    }

    LaunchedEffect(mediaFiles.size, playIndex) {
        if (mediaFiles.size > 1) {
            delay(SLIDE_INTERVAL_MS)
            playIndex = (playIndex + 1) % mediaFiles.size
        }
    }

    if (playIndex >= mediaFiles.size) playIndex = 0
    val currentFile = mediaFiles.getOrNull(playIndex)

    Box(
        modifier = modifier
            .background(Color(0xFFFFFBF2), RoundedCornerShape(34.dp))
            .border(1.dp, Color(0xFFE4D8C4), RoundedCornerShape(34.dp))
            .padding(24.dp)
    ) {
        if (currentFile == null) {
            EmptyReceivedMediaHint(Modifier.align(Alignment.Center))
        } else {
            AnimatedContent(
                targetState = currentFile.absolutePath,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "mediaSlide",
                modifier = Modifier.fillMaxSize()
            ) {
                PlayingReceivedMedia(
                    file = currentFile,
                    currentIndex = playIndex + 1,
                    totalCount = mediaFiles.size,
                    onVideoFinished = {
                        if (mediaFiles.size > 1) playIndex = (playIndex + 1) % mediaFiles.size
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun PlayingReceivedMedia(
    file: File,
    currentIndex: Int,
    totalCount: Int,
    onVideoFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when {
            file.isImageFile() -> ImageMedia(file = file, modifier = Modifier.fillMaxSize())
            file.isVideoFile() -> VideoMedia(file = file, onFinished = onVideoFinished, modifier = Modifier.fillMaxSize())
            else -> MediaTextHint("暂不支持此素材：${file.name}")
        }

        Text(
            text = "$currentIndex / $totalCount",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF64736A),
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

@Composable
private fun ImageMedia(file: File, modifier: Modifier = Modifier) {
    val bitmap = remember(file.absolutePath, file.lastModified()) {
        BitmapFactory.decodeFile(file.absolutePath)
    }
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = file.name,
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    } else {
        MediaTextHint("图片读取失败：${file.name}")
    }
}

@Composable
private fun VideoMedia(
    file: File,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            VideoView(context).apply {
                setVideoURI(Uri.fromFile(file))
                setOnPreparedListener { player ->
                    player.isLooping = false
                    start()
                }
                setOnCompletionListener { onFinished() }
                setOnErrorListener { _, _, _ ->
                    onFinished()
                    true
                }
            }
        },
        update = { videoView ->
            videoView.setVideoURI(Uri.fromFile(file))
            videoView.setOnPreparedListener { player ->
                player.isLooping = false
                videoView.start()
            }
            videoView.setOnCompletionListener { onFinished() }
            videoView.setOnErrorListener { _, _, _ ->
                onFinished()
                true
            }
        }
    )
}

@Composable
private fun EmptyReceivedMediaHint(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        PostBirdWaitingIcon()
        Text(
            text = "还没有收到照片",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFF2E3A32)
        )
        Text(
            text = "手机端发送照片或视频邮件后，在设置页点击“立即检查”即可接收。",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF66756A),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MediaTextHint(text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF66756A),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PostBirdWaitingIcon() {
    Box(
        modifier = Modifier
            .background(Color(0xFFF7DFA2), RoundedCornerShape(58.dp))
            .padding(horizontal = 28.dp, vertical = 20.dp)
    ) {
        Text("✉", style = MaterialTheme.typography.headlineLarge, color = Color(0xFFE8B85F))
    }
}

private fun File.isImageFile(): Boolean {
    val lower = name.lowercase()
    return listOf(".jpg", ".jpeg", ".png", ".webp", ".gif").any { lower.endsWith(it) }
}

private fun File.isVideoFile(): Boolean {
    val lower = name.lowercase()
    return listOf(".mp4", ".mov", ".m4v", ".3gp").any { lower.endsWith(it) }
}

private const val MEDIA_REFRESH_INTERVAL_MS = 5000L
private const val SLIDE_INTERVAL_MS = 8000L
