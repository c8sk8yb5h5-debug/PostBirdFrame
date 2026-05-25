package com.postbird.frame.matepad

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PostBirdFrameTheme {
                MatePadPreviewHomeScreen()
            }
        }
    }
}

@Composable
private fun PostBirdFrameTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF78D6FF),
            secondary = Color(0xFF9EE6B8),
            background = Color(0xFF050607),
            surface = Color(0xFF101317),
            onPrimary = Color(0xFF071016),
            onSecondary = Color(0xFF071016),
            onBackground = Color.White,
            onSurface = Color.White
        ),
        content = content
    )
}
