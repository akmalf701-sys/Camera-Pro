package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "media_items")
data class MediaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val fileName: String,
    val mediaType: String, // "PHOTO" or "VIDEO"
    val timestamp: Long = System.currentTimeMillis(),
    val fileSize: Long = 0L,
    val width: Int = 1920,
    val height: Int = 1080,
    val filterUsed: String = "NORMAL",
    val iso: Int = 200,
    val shutterSpeed: String = "1/125s",
    val isStabilized: Boolean = true,
    val isNightMode: Boolean = false,
    val cloudSyncStatus: String = "PENDING", // "SYNCED", "SYNCING", "PENDING"
    val cloudSyncTimestamp: Long = 0L,
    val durationSeconds: Int = 0 // for video
)
