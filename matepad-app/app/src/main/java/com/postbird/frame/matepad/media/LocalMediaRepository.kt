package com.postbird.frame.matepad.media

class LocalMediaRepository {
    private val items = mutableMapOf<String, LocalMediaItem>()

    fun addOrIgnore(item: LocalMediaItem): Boolean {
        if (items.containsKey(item.id)) return false
        items[item.id] = item
        return true
    }

    fun exists(id: String): Boolean {
        return items.containsKey(id)
    }

    fun listAll(): List<LocalMediaItem> {
        return items.values.sortedByDescending { it.receivedAt }
    }

    fun clearMemoryIndex() {
        items.clear()
    }
}
