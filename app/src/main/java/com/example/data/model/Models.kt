package com.example.data.model

data class MediaItem(
    val fileId: String,
    val fileUniqueId: String? = null,
    val type: String = "photo", // "photo", "video", "audio", "voice"
    val caption: String? = null,
    val width: Int? = null,
    val height: Int? = null,
    val duration: Int? = null,
    val fileSize: Long? = null,
    val url: String? = null
)

data class Announcement(
    val id: Long = 0,
    val senderTelegramId: Long,
    val senderUsername: String?,
    val senderDisplayName: String,
    val contentType: String, // "TEXT", "PHOTO", "ALBUM", "VIDEO", "AUDIO", "VOICE"
    val textContent: String?,
    val mediaGroupId: String? = null,
    val mediaItems: List<MediaItem> = emptyList(),
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val createdAt: String,
    val updatedAt: String,
    val publishedAt: String? = null,
    val publishedMessageIds: List<Long> = emptyList(),
    val rejectionReason: String? = null,
    val adminNotes: String? = null,
    val publishedBy: String? = null
)

data class Friend(
    val id: Long = 0,
    val telegramId: Long = 0,
    val username: String? = null,
    val displayName: String = "",
    val addedAt: String = "",
    val isActive: Boolean = true,
    val notes: String? = null,
    val submissionCount: Int = 0
)

data class SystemLog(
    val id: Long = 0,
    val timestamp: String,
    val level: String, // "INFO", "SUCCESS", "WARN", "ERROR"
    val category: String,
    val message: String,
    val details: String? = null
)

data class DashboardStats(
    val totalAnnouncements: Int = 0,
    val pendingAnnouncements: Int = 0,
    val publishedAnnouncements: Int = 0,
    val rejectedAnnouncements: Int = 0,
    val totalFriends: Int = 0,
    val botConnected: Boolean = false,
    val targetChannel: String = "@SolixxTR",
    val serverTime: String = ""
)

data class ServerConfig(
    val serverUrl: String = "http://10.0.2.2:8000",
    val apiKey: String = "",
    val adminTelegramId: Long = 0L,
    val targetChannel: String = "@SolixxTR",
    val isLiveConnected: Boolean = false,
    val latencyMs: Long? = null,
    val isLocalMode: Boolean = true
)
