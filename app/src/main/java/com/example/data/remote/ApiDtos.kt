package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class MediaItemDto(
    @Json(name = "file_id") val fileId: String,
    @Json(name = "file_unique_id") val fileUniqueId: String? = null,
    @Json(name = "type") val type: String = "photo",
    @Json(name = "caption") val caption: String? = null,
    @Json(name = "width") val width: Int? = null,
    @Json(name = "height") val height: Int? = null,
    @Json(name = "duration") val duration: Int? = null,
    @Json(name = "file_size") val fileSize: Long? = null,
    @Json(name = "url") val url: String? = null
)

@JsonClass(generateAdapter = true)
data class AnnouncementDto(
    @Json(name = "id") val id: Long,
    @Json(name = "sender_telegram_id") val senderTelegramId: Long,
    @Json(name = "sender_username") val senderUsername: String? = null,
    @Json(name = "sender_display_name") val senderDisplayName: String,
    @Json(name = "content_type") val contentType: String,
    @Json(name = "text_content") val textContent: String? = null,
    @Json(name = "media_group_id") val mediaGroupId: String? = null,
    @Json(name = "media_items") val mediaItems: List<MediaItemDto> = emptyList(),
    @Json(name = "status") val status: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "published_at") val publishedAt: String? = null,
    @Json(name = "published_message_ids") val publishedMessageIds: List<Long> = emptyList(),
    @Json(name = "rejection_reason") val rejectionReason: String? = null,
    @Json(name = "admin_notes") val adminNotes: String? = null,
    @Json(name = "published_by") val publishedBy: String? = null
)

@JsonClass(generateAdapter = true)
data class FriendDto(
    @Json(name = "id") val id: Long,
    @Json(name = "telegram_id") val telegramId: Long,
    @Json(name = "username") val username: String? = null,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "added_at") val addedAt: String,
    @Json(name = "is_active") val isActive: Int,
    @Json(name = "notes") val notes: String? = null,
    @Json(name = "submission_count") val submissionCount: Int? = 0
)

@JsonClass(generateAdapter = true)
data class FriendCreateDto(
    @Json(name = "telegram_id") val telegramId: Long,
    @Json(name = "display_name") val displayName: String? = null,
    @Json(name = "username") val username: String? = null,
    @Json(name = "notes") val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class ApproveDto(
    @Json(name = "published_by") val publishedBy: String? = null
)

@JsonClass(generateAdapter = true)
data class RejectDto(
    @Json(name = "rejection_reason") val rejectionReason: String,
    @Json(name = "admin_notes") val adminNotes: String? = null
)

@JsonClass(generateAdapter = true)
data class EditDto(
    @Json(name = "new_text") val newText: String,
    @Json(name = "admin_notes") val adminNotes: String? = null
)

@JsonClass(generateAdapter = true)
data class SystemStatusDto(
    @Json(name = "online") val online: Boolean,
    @Json(name = "bot_connected") val botConnected: Boolean,
    @Json(name = "target_channel") val targetChannel: String,
    @Json(name = "total_announcements") val totalAnnouncements: Int,
    @Json(name = "pending_announcements") val pendingAnnouncements: Int,
    @Json(name = "published_announcements") val publishedAnnouncements: Int,
    @Json(name = "rejected_announcements") val rejectedAnnouncements: Int,
    @Json(name = "total_friends") val totalFriends: Int,
    @Json(name = "server_time") val serverTime: String,
    @Json(name = "database_ok") val databaseOk: Boolean
)

@JsonClass(generateAdapter = true)
data class LogDto(
    @Json(name = "id") val id: Long,
    @Json(name = "timestamp") val timestamp: String,
    @Json(name = "level") val level: String,
    @Json(name = "category") val category: String,
    @Json(name = "message") val message: String,
    @Json(name = "details") val details: String? = null
)

@JsonClass(generateAdapter = true)
data class ApiResponseDto(
    @Json(name = "success") val success: Boolean = true,
    @Json(name = "message") val message: String? = null
)
