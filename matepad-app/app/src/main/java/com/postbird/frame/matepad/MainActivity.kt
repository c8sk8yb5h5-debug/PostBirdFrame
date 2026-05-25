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
                MatePadPreviewHomeScreenV3()
            }
        }
    }
}

@Composable
private fun PostBirdFrameTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF006D62),
            secondary = Color(0xFFEFF8F4),
            background = Color.Black,
            surface = Color(0xFFEFF8F4),
            onPrimary = Color.White,
            onSecondary = Color(0xFF102427),
            onBackground = Color.White,
            onSurface = Color(0xFF102427)
        ),
        content = content
    )
}
