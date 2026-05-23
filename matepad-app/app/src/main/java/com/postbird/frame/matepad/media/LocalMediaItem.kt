package com.postbird.frame.matepad.media

data class LocalMediaItem(
    val id: String,
    val fileName: String,
    val localPath: String,
    val mediaType: LocalMediaType,
    val sourceMailFrom: String,
    val sourceMailSubject: String,
    val sourceMessageNumber: Int,
    val attachmentName: String,
    val receivedAt: Long,
    val fileSizeBytes: Long
)
