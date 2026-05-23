package com.postbird.frame.matepad.media

enum class LocalMediaType {
    IMAGE,
    VIDEO,
    UNKNOWN;

    companion object {
        fun fromFileName(fileName: String): LocalMediaType {
            val extension = fileName.substringAfterLast('.', missingDelimiterValue = "").lowercase()
            return when (extension) {
                "jpg", "jpeg", "png", "webp" -> IMAGE
                "mp4", "mov", "m4v" -> VIDEO
                else -> UNKNOWN
            }
        }
    }
}
