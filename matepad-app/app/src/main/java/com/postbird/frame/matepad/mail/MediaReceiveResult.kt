package com.postbird.frame.matepad.mail

data class MediaReceiveResult(
    val success: Boolean,
    val message: String,
    val savedCount: Int = 0,
    val skippedCount: Int = 0
)
