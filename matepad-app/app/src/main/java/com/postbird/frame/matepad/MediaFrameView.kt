package com.postbird.frame.matepad

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.postbird.frame.matepad.mail.MediaReceiveStore
import java.io.File
import kotlinx.coroutines.delay

@Composable
fun ReceivedMediaFrame(
    modifier: Modifier = Modifier,
    showCounter: Boolean = false,
    isPlaying: Boolean = true,
    selectedFilePath: String? = null
) {
    val context = LocalContext.current
    var refreshTick by remember { mutableIntStateOf(0) }
    var playIndex by remember { mutableIntStateOf(0) }
    var manualControlTick by remember { mutableLongStateOf(0L) }
    var dragTotal by remember { mutableFloatStateOf(0f) }
    val mediaFiles = remember(refreshTick) { MediaReceiveStore(context).listMediaFiles() }

    fun moveToNext() {
        if (mediaFiles.size > 1) {
            playIndex = (playIndex + 1) % mediaFiles.size
            manualControlTick = System.currentTimeMillis()
        }
    }

    fun moveToPrevious() {
        if (mediaFiles.size > 1) {
            playIndex = (playIndex - 1 + mediaFiles.size) % mediaFiles.size
            manualControlTick = System.currentTimeMillis()
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(MEDIA_REFRESH_INTERVAL_MS)
            refreshTick++
        }
    }

    LaunchedEffect(selectedFilePath, mediaFiles.size) {
        if (!selectedFilePath.isNullOrBlank()) {
            val targetIndex = mediaFiles.indexOfFirst { it.absolutePath == selectedFilePath }
            if (targetIndex >= 0) {
                playIndex = targetIndex
                manualControlTick = System.currentTimeMillis()
            }
        }
    }

    LaunchedEffect(mediaFiles.size, playIndex, manualControlTick, isPlaying) {
        if (isPlaying && mediaFiles.size > 1) {
            val waitTime = if (manualControlTick > 0L) MANUAL_PAUSE_INTERVAL_MS else SLIDE_INTERVAL_MS
            delay(waitTime)
            playIndex = (playIndex + 1) % mediaFiles.size
        }
    }

    if (playIndex >= mediaFiles.size) playIndex = 0
    val currentFile = mediaFiles.getOrNull(playIndex)

    Box(
        modifier = modifier
            .background(Color.Black)
            .pointerInput(mediaFiles.size) {
                detectHorizontalDragGestures(
                    onDragStart = { dragTotal = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragTotal += dragAmount
                    },
                    onDragEnd = {
                        when {
                            dragTotal <= -SWIPE_THRESHOLD_PX -> moveToNext()
                            dragTotal >= SWIPE_THRESHOLD_PX -> moveToPrevious()
                        }
                        dragTotal = 0f
                    },
                    onDragCancel = { dragTotal = 0f }
                )
            }
    ) {
        if (currentFile == null) {
            EmptyReceivedMediaHint(Modifier.align(Alignment.Center))
        } else {
            PlayingReceivedMedia(
                file = currentFile,
                currentIndex = playIndex + 1,
                totalCount = mediaFiles.size,
                showCounter = showCounter,
                onVideoFinished = { if (isPlaying) moveToNext() },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun PlayingReceivedMedia(
    file: File,
    currentIndex: Int,
    totalCount: Int,
    showCounter: Boolean,
    onVideoFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        when {
            file.isImageFile() -> ImageMedia(file = file, modifier = Modifier.fillMaxSize())
            file.isVideoFile() -> VideoMedia(file = file, onFinished = onVideoFinished, modifier = Modifier.fillMaxSize())
            else -> MediaTextHint("暂不支持此素材：${file.name}")
        }

        if (showCounter) {
            Text(
                text = "$currentIndex / $totalCount",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.58f),
                modifier = Modifier.align(Alignment.BottomEnd).padding(18.dp)
            )
        }
    }
}

@Composable
private fun ImageMedia(file: File, modifier: Modifier = Modifier) {
    val bitmap = remember(file.absolutePath, file.lastModified()) {
        decodeSampledBitmap(file, MAIN_IMAGE_MAX_SIZE)
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("✉", style = MaterialTheme.typography.headlineLarge, color = Color.White.copy(alpha = 0.38f))
        Text(
            text = "暂无照片 / 视频",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White.copy(alpha = 0.72f)
        )
        Text(
            text = "打开设置，点击手动收件。",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.46f),
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
            color = Color.White.copy(alpha = 0.62f),
            textAlign = TextAlign.Center
        )
    }
}

private fun decodeSampledBitmap(file: File, maxSize: Int): Bitmap? {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while ((bounds.outWidth / sample) > maxSize || (bounds.outHeight / sample) > maxSize) {
            sample *= 2
        }
        BitmapFactory.decodeFile(
            file.absolutePath,
            BitmapFactory.Options().apply {
                inSampleSize = sample.coerceAtLeast(1)
                inPreferredConfig = Bitmap.Config.RGB_565
            }
        )
    } catch (_: Exception) {
        null
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

private const val MEDIA_REFRESH_INTERVAL_MS = 60000L
private const val SLIDE_INTERVAL_MS = 8000L
private const val MANUAL_PAUSE_INTERVAL_MS = 12000L
private const val SWIPE_THRESHOLD_PX = 80f
private const val MAIN_IMAGE_MAX_SIZE = 1920
