package com.postbird.frame.matepad

import android.graphics.BitmapFactory
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.postbird.frame.matepad.mail.MediaReceiveStore
import java.io.File

@Composable
fun ReceivedMediaFrame(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val mediaFiles = remember { MediaReceiveStore(context).listMediaFiles() }
    val latestFile = mediaFiles.firstOrNull()

    Box(
        modifier = modifier
            .background(Color(0xFFFFFBF2), RoundedCornerShape(34.dp))
            .border(1.dp, Color(0xFFE4D8C4), RoundedCornerShape(34.dp))
            .padding(24.dp)
    ) {
        if (latestFile == null) {
            EmptyReceivedMediaHint(Modifier.align(Alignment.Center))
        } else {
            LatestReceivedMedia(
                file = latestFile,
                totalCount = mediaFiles.size,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun LatestReceivedMedia(
    file: File,
    totalCount: Int,
    modifier: Modifier = Modifier
) {
    val lowerName = file.name.lowercase()
    val isImage = listOf(".jpg", ".jpeg", ".png", ".webp").any { lowerName.endsWith(it) }

    Box(modifier = modifier) {
        if (isImage) {
            val bitmap = remember(file.absolutePath, file.lastModified()) {
                BitmapFactory.decodeFile(file.absolutePath)
            }
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = file.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                MediaTextHint("图片读取失败：${file.name}")
            }
        } else {
            MediaTextHint("已收到视频：${file.name}\n视频播放将在下一轮接入。")
        }

        Text(
            text = "已收到 $totalCount 个素材",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF64736A),
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
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
